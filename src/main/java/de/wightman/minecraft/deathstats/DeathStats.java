package de.wightman.minecraft.deathstats;

import de.wightman.minecraft.deathstats.db.DeathsDB;
import de.wightman.minecraft.deathstats.event.DeathSoundEvents;
import de.wightman.minecraft.deathstats.event.OverlayUpdateEvent;
import de.wightman.minecraft.deathstats.event.NewHighScoreEvent;
import de.wightman.minecraft.deathstats.gui.ConfigScreen;
import de.wightman.minecraft.deathstats.gui.TopDeathStatsScreen;
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
import java.sql.SQLException;
import java.util.*;

@Mod(DeathStats.MOD_ID)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class DeathStats {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeathStats.class);

    public static final String MOD_ID = "deathstats";

    private static DeathStats INSTANCE;

    private File deathsFile;
    private boolean isHighScore = false;

    private static final String KEY_MAX = "session_death_max";
    private static final String KEY_CURRENT = "current_session_deaths";
    private static final String KEY_IS_VISIBLE = "is_visible";

    // the name of the sessions created when people start and end their world
    public static final String DEFAULT_SESSION = "default";

    private DeathsDB db;
    private String worldName;

    public DeathStats() {
        //Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "ANY", (remote, isServer) -> true));

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::init);

        INSTANCE = this;
    }

    @OnlyIn(Dist.CLIENT)
    private void init() {
        LOGGER.info("Starting DeathStats");

        String home = System.getProperty("user.home");
        deathsFile = new File(home, "minecraft_deaths.dat"); // FIXED

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

            db = new DeathsDB();
            db.startSession(DEFAULT_SESSION);

            Minecraft client = Minecraft.getInstance();
            IntegratedServer ss = client.getSingleplayerServer();
            if (ss != null) {
                worldName = ss.getWorldData().getLevelName();
            } else {
                ServerData cs = client.getCurrentServer();
                worldName = cs.name;
            }

            LOGGER.debug("startSession {} {} {}", Minecraft.getInstance().player, Minecraft.getInstance().level, worldName);
        } catch (SQLException exception) {
            LOGGER.error("Cannot open {}", deathsFile.getAbsolutePath(), exception);
            MutableComponent c = Component.literal("ERROR: Cannot open " + deathsFile.getAbsolutePath());
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
        return db.getMaxDeathsPerSession(DEFAULT_SESSION);
    }

    public long getCurrent() {
        if (db == null) return -1;
        // needs to cache and use death events
        return db.getActiveDeathsSession(DEFAULT_SESSION);
    }

    public void setMax(final int max) {
        // TODO Not possible need to fake death log
    }

    public boolean isVisible() {
        return true;
    }

    public void setVisible(boolean visible) {
        // TODO
    }

    public void triggerHighScoreEvent() {
        MinecraftForge.EVENT_BUS.post(new NewHighScoreEvent());
    }

    public void triggerNewDeathEvent() {
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

    public File getDeathsFile() {
        return deathsFile;
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

        DeathRecord dr = new DeathRecord(worldName, dimension, deathMessageKey, killedByKey, killedByName, argb);

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
                triggerNewDeathEvent();
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

        triggerNewDeathEvent();
    }

    public void debugSessionTable() {
        if (db == null) return;
        db.debugSessionTable();
    }

    public void debugDeathLogTable() {
        if (db == null) return;
        db.debugDeathLogTable();
    }
}
