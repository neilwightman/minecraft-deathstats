package de.wightman.minecraft.deathstats;

import de.wightman.minecraft.deathstats.db.DeathsDB;
import de.wightman.minecraft.deathstats.event.DeathSoundEvents;
import de.wightman.minecraft.deathstats.event.OverlayUpdateEvent;
import de.wightman.minecraft.deathstats.event.NewHighScoreEvent;
import de.wightman.minecraft.deathstats.gui.ConfigScreen;
import de.wightman.minecraft.deathstats.gui.TopDeathStatsScreen;
import de.wightman.minecraft.deathstats.record.DeathRecord;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;

import static de.wightman.minecraft.deathstats.record.DeathRecord.NOT_SET;

@Mod(DeathStats.MOD_ID)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class DeathStats {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeathStats.class);

    public static final String MOD_ID = "deathstats";

    private static DeathStats INSTANCE;

    private boolean isHighScore = false;

    public static final String KEY_MAX = "session_death_max";
    public static final String KEY_IS_VISIBLE = "is_visible";

    // the name of the sessions created when people start and end their world
    public static final String DEFAULT_SESSION = "default";

    private DeathsDB db;
    private String worldName;
    private boolean isVisible = true;
    // Old max stores the max deaths like it would have been in version 1.0.x
    private long oldMax = -1;

    public DeathStats() {
        //Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "ANY", (remote, isServer) -> true));

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::init);

        INSTANCE = this;
    }

    @OnlyIn(Dist.CLIENT)
    private void init() {
        LOGGER.info("Starting DeathStats");

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        DeathSoundEvents.registerSoundEvent(eventBus);

        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> new ConfigScreen(screen)));
    }

    public static DeathStats getInstance() {
        return INSTANCE;
    }

    // Handle start, leave and deaths

    public void startSession() {
        try {
            isHighScore = false;

            String home = System.getProperty("user.home");
            File deathStatsDir = new File(home, "deathstats"); // FIXED
            deathStatsDir.mkdirs();
            Path path = Path.of(deathStatsDir.getPath(), "sqlite.db");

            db = new DeathsDB(path);
            db.startSession(DEFAULT_SESSION);

            Minecraft client = Minecraft.getInstance();
            IntegratedServer ss = client.getSingleplayerServer();
            if (ss != null) {
                String levelName = ss.getWorldData().getLevelName();
                ServerLevel serverLevel = ss.getLevel(client.level.dimension());
                long seed = serverLevel.getSeed();
                worldName = levelName + "/" + seed;
            } else {
                ServerData cs = client.getCurrentServer();
                worldName = cs.name + "/" + cs.ip;
            }

            final String visibleStr = db.getConfig(KEY_IS_VISIBLE);
            isVisible = (visibleStr == null) ? true : Boolean.valueOf(visibleStr);

            final String maxStr = db.getConfig(KEY_MAX);
            if (maxStr != null) {
                oldMax = Long.valueOf(maxStr);
            }

            LOGGER.debug("startSession {} {} {}", Minecraft.getInstance().player, Minecraft.getInstance().level, worldName);
        } catch (SQLException exception) {
            LOGGER.error("Cannot open sqlite3 db", exception);
            MutableComponent c = Component.literal("ERROR: Cannot open sqlite3 db\nError:%s".formatted(exception.getMessage()));
            Minecraft.getInstance().player.sendSystemMessage(c);
        }
    }

    public void endSession() {
        LOGGER.debug("endSession");

        try {
            if (db != null) { // end session is called before a startSession.
                db.endSession(DEFAULT_SESSION);
            }
        } catch (SQLException sql) {
            LOGGER.warn("Cannot end session in the db", sql);
        }
    }

    public long getMax() {
        if (db == null) return -1;
        // needs to cache and use death events
        long max = db.getMaxDeathsPerSession(DEFAULT_SESSION);
        return Math.max(oldMax, max);
    }

    public long getCurrent() {
        if (db == null) return -1;
        // needs to cache and use death events
        return db.getActiveDeathsSession(DEFAULT_SESSION);
    }

    public void setMax(final long max) throws SQLException {
        if (db == null) return;
        db.setConfig(KEY_MAX, String.valueOf(max));
        oldMax = max;
        triggerOverlayUpdateEvent();
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
        if (db == null) return;
        try {
            db.setConfig(KEY_IS_VISIBLE, String.valueOf(visible));
        } catch (SQLException e) {
            LOGGER.warn("Cannot set visible config value", e);
        }
    }

    public void triggerHighScoreEvent() {
        MinecraftForge.EVENT_BUS.post(new NewHighScoreEvent());
    }

    public void triggerOverlayUpdateEvent() {
        MinecraftForge.EVENT_BUS.post(new OverlayUpdateEvent());
    }

    @OnlyIn(Dist.CLIENT)
    public void playHighScoreSound() {
        LOGGER.debug("playHishScoreSound {} {}", Thread.currentThread().getName(), Thread.currentThread().getId());

        SoundEvent s = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(MOD_ID, "high_score"));
        if (s == null) {
            LOGGER.error("high_score sound is null");
            return;
        }

        SimpleSoundInstance ssi = SimpleSoundInstance.forUI(s, 1.0f);

        Minecraft.getInstance().getSoundManager().playDelayed(ssi, 1);
    }

    public boolean isHighScore() {
        return isHighScore;
    }

    public @Nullable Path getDbPath() {
        if (db == null) return null;
        return db.getPath();
    }

    // Register user events

    @SubscribeEvent
    public void onJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        LOGGER.info("onJoin({})", event.getPlayer());
        startSession();
    }

    @SubscribeEvent
    public void onLeave(ClientPlayerNetworkEvent.LoggingOut event) {
        LOGGER.info("onLeave({})", event.getPlayer());
        endSession();
    }

    private void updateHighScore() {
        if (db == null) return;

        // update deaths
        long current = db.getActiveDeathsSession(DEFAULT_SESSION);
        long max = db.getMaxDeathsPerSession(DEFAULT_SESSION);

        LOGGER.debug("death current={}", current);

        if (current > max) {
            if (!isHighScore) {
                triggerHighScoreEvent();
            }

            isHighScore = true;
        } else {
            isHighScore = false;
        }
    }

    @SubscribeEvent
    public void playHighScoreSoundOnEvent(final NewHighScoreEvent event) {
        if (isVisible()) playHighScoreSound();
    }

    private void logDeath(String worldName, String dimension, String deathMessageKey, @Nullable String killedByKey, @Nullable String killedByName, int argb) {
        if (db == null) return;

        Objects.requireNonNull(deathMessageKey);

        LOGGER.info("logDeath({},{},{},{},{})", worldName, dimension, deathMessageKey, killedByKey, killedByName);

        DeathRecord dr = new DeathRecord(NOT_SET, worldName, dimension, deathMessageKey, killedByKey, killedByName, argb, System.currentTimeMillis());

        try {
            db.newDeath(dr);
        } catch (SQLException e) {
            LOGGER.warn("Cannot store death log to db", e);
        }
    }

    public void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket clientPlayerCombatKillPacket) {
        LOGGER.debug("handlePlayerCombatKill({})", clientPlayerCombatKillPacket);
        Entity entity = Minecraft.getInstance().level.getEntity(clientPlayerCombatKillPacket.getPlayerId());
        // Only process LocalPlayer
        if (entity == Minecraft.getInstance().player) {
            Component msg = clientPlayerCombatKillPacket.getMessage();
            ComponentContents contents = msg.getContents();
            if (contents instanceof TranslatableContents tc) {
                StringBuilder str = new StringBuilder();
                tc.visit((part) -> {
                    str.append(part);
                    return Optional.empty();
                });

                LOGGER.info("Death {} {}", tc, str);

                String key = tc.getKey();
                if (key.startsWith("death.")) {
                    Object killedBy = null;

                    Object[] args = tc.getArgs();

                    if (key.endsWith(".player")
                            || key.endsWith(".item")) {
                        killedBy = args[1];  // %2
                    }

                    if (args.length >= 2) {
                        killedBy = args[1];  // %2 on all death. messages is the player or mob name
                    }

                    LOGGER.debug("{} {}", key, killedBy);
                    String killedByKey = null;  // localised key of item which killed the player
                    String killedByStr = null;  // the name of the mob for named mobs and players
                    int intARGB = ChatFormatting.WHITE.getColor();

                    ResourceKey<Level> dim = Minecraft.getInstance().level.dimension();
                    ResourceLocation rs = dim.location();
                    String dimStr = rs.getPath();

                    if (killedBy instanceof MutableComponent mc) {
                        ComponentContents killedByContents = mc.getContents();
                        Style style = mc.getStyle();
                        TextColor color = style.getColor();
                        if (color != null) intARGB = color.getValue();

                        if (killedByContents instanceof TranslatableContents killedByTc) {
                            killedByKey = killedByTc.getKey();
                        }
                        if (killedByContents instanceof LiteralContents killedByLc) {
                            killedByStr = killedByLc.text();
                        }
                    }

                    logDeath(worldName, dimStr, key, killedByKey, killedByStr, intARGB);
                }

                updateHighScore();
                triggerOverlayUpdateEvent();
            }
        }
    }

    public List<Long> getDeathsPerSession(final int sessionId) {
        if (db == null) return Collections.emptyList();
        return db.getDeathsPerSession(sessionId);
    }

    public int getActiveSessionId() {
        if (db == null) return -1;
        return db.getActiveSessionId();
    }

    public List<TopDeathStatsScreen.DeathLeaderBoardEntry> getLeaderBoardForSession(final int sessionId) {
        if (db == null) return Collections.emptyList();
        return db.getLeaderBoardForSession(sessionId);
    }

    public void resumeLastSession() {
        if (db == null) return;
        db.resumeLastSession();

        triggerOverlayUpdateEvent();
    }

    public void debugSessionTable() {
        if (db == null) return;
        db.debugSessionTable();
    }

    public void debugDeathLogTable() {
        if (db == null) return;
        db.debugDeathLogTable();
    }

    public void debugConfigTable() {
        if (db == null) return;
        db.debugConfigTable();
    }
}
