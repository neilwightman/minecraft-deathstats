package de.wightman.minecraft.deathstats;

import de.wightman.minecraft.deathstats.event.NewHighScoreEvent;
import de.wightman.minecraft.deathstats.gui.ConfigScreen;
import de.wightman.minecraft.deathstats.gui.DeathSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
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
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.MVStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;

@Mod(DeathStats.MOD_ID)
public class DeathStats {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeathStats.class);

    public static final String MOD_ID = "deathstats";

    private static DeathStats INSTANCE;

    private File deathsFile;
    private MVStore store;
    private MVMap<String, Object> properties;
    private boolean isHighScore = false;

    private static final String KEY_MAX = "session_death_max";
    private static final String KEY_CURRENT = "current_session_deaths";
    private static final String KEY_IS_VISIBLE = "is_visible";
    private MVMap<Long, String> deathLog; // cannot store deathrecord classes as mv2 jar in jar class loader cant access our classes.

    public DeathStats() {
        //Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "ANY", (remote, isServer) -> true));

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::init);

        INSTANCE = this;
    }

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
            store = MVStore.open(deathsFile.getAbsolutePath());

            // Basic key value map
            properties = store.openMap("minecraft_deaths"); // FIXED
            // allow player to clear and define what a current session is.
            Integer current = (Integer) properties.putIfAbsent(KEY_CURRENT, 0);
            if (current == null) {
                current = 0;
            }

            Integer max = (Integer) properties.putIfAbsent(KEY_MAX, 0);
            if (max == null) {
                max = 0;
            }

            Boolean visible = (Boolean) properties.putIfAbsent(KEY_IS_VISIBLE, true);
            if (visible == null) {
                visible = true;
            }

            isHighScore = false;

            deathLog = store.openMap("minecraft_deaths_log"); // FIXED

            LOGGER.debug("startSession {} {}", Minecraft.getInstance().player, Minecraft.getInstance().level);
            LOGGER.info("deathstats max={}, current={} visible={} log={}", max, current, visible, deathLog.size());
        } catch (MVStoreException mvStoreException) {
            LOGGER.error("Cannot open {}", deathsFile.getAbsolutePath(), mvStoreException);
            MutableComponent c = Component.literal("ERROR: Cannot open " + deathsFile.getAbsolutePath());
            Minecraft.getInstance().player.sendSystemMessage(c);
        }
    }

    public void endSession() {
        LOGGER.debug("endSession");
        if (store != null) {
            store.close();
        }

        store = null;
        properties = null;
        deathLog = null;
    }

    public int getMax() {
        if (properties == null) return -1;
        return (Integer) properties.get(KEY_MAX);
    }

    public int getCurrent() {
        if (properties == null) return - 1;
        return (Integer) properties.get(KEY_CURRENT);
    }

    public void setMax(final int max) {
        if (properties == null) return;
        properties.put(KEY_MAX, max);
    }

    public boolean isVisible() {
        if (properties == null) return true;
        return (Boolean) properties.get(KEY_IS_VISIBLE);
    }

    public void setVisible(boolean visible) {
        if (properties == null) return;
        properties.put(KEY_IS_VISIBLE, visible);
    }


    public void setCurrent(final int current) {
        if (properties == null) return;

        properties.put(KEY_CURRENT, current);

        Integer max = (Integer) properties.get(KEY_MAX);

        if (current > max) {
            properties.put(KEY_MAX, current);

            if (!isHighScore) {
                if (isVisible()) playHighScoreSound();
                triggerHighScoreEvent();
            }

            isHighScore = true;
        } else {
            isHighScore = false;
        }
    }

    public void triggerHighScoreEvent() {
        MinecraftForge.EVENT_BUS.post(NewHighScoreEvent.HIGH_SCORE_EVENT);
    }

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

    private void increaseCounter() {
        if (properties == null) return;

        // update deaths
        Integer current = (Integer) properties.get(KEY_CURRENT);
        current += 1;

        LOGGER.debug("death current={}", current);

        setCurrent(current);
    }

    private void logDeath(String deathMessageKey, String killedByKey, String killedByName) {
        if (deathLog == null) return;

        LOGGER.info("logDeath({},{},{})", deathMessageKey, killedByKey, killedByName);

        DeathRecord dr = new DeathRecord(deathMessageKey, killedByKey, killedByName);
        deathLog.append(System.currentTimeMillis(), dr.toJsonString());
    }

    public MVMap<Long, String> getDeathLog() {
        return deathLog;
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

                LOGGER.info("Death {} {}", tc, str.toString());

                String key = tc.getKey();
                if (key.startsWith("death.")) {
                    Object killedBy = null;

                    Object[] args = tc.getArgs();

                    if (key.endsWith(".player")
                        || key.endsWith(".item")) {
                        killedBy = args[1];  // %2
                    }

                    if (args.length >= 2) {
                        killedBy= args[1];  // %2 on all death. messages is the player or mob name
                    }

                    LOGGER.debug("{} {}", key, killedBy);
                    String killedByKey = null;  // localised key of item which killed the player
                    String killedByStr = null;  // the name of the mob for named mobs and players

                    if (killedBy instanceof MutableComponent mc) {
                        ComponentContents killedByContents = mc.getContents();
                        if (killedByContents instanceof TranslatableContents killedByTc) {
                            killedByKey = killedByTc.getKey();
                        }
                        if (killedByContents instanceof LiteralContents killedByLc) {
                            killedByStr = killedByLc.text();
                        }
                    }

                    logDeath(key, killedByKey, killedByStr);
                }

                increaseCounter();
            }
        }
    }
}
