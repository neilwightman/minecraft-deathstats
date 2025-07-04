package de.wightman.minecraft.deathstats;

import de.wightman.minecraft.deathstats.event.NewHighScoreEvent;
import de.wightman.minecraft.deathstats.gui.DeathSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.MVStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@Mod(value = DeathStats.MOD_ID, dist = Dist.CLIENT)
public class DeathStats {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeathStats.class);

    public static final String MOD_ID = "deathstats";

    private static DeathStats INSTANCE;

    private File deathsFile;
    private MVStore store;
    private MVMap<String, Object> map;
    private boolean isHighScore = false;

    private static final String KEY_MAX = "session_death_max";
    private static final String KEY_CURRENT = "current_session_deaths";
    private static final String KEY_IS_VISIBLE = "is_visible";


    public DeathStats(IEventBus modBus, ModContainer container) {
        LOGGER.info("Starting DeathStats");

        String home = System.getProperty("user.home");
        deathsFile = new File(home, "minecraft_deaths.dat"); // FIXED

        // Register ourselves for server and other game events we are interested in
        NeoForge.EVENT_BUS.register(this);
        // Client side commands
        NeoForge.EVENT_BUS.register(ClientCommands.class);
        // Hud gui
        modBus.register(ClientSetupHandler.class);
        // Sounds
        DeathSoundEvents.registerSoundEvent(modBus);

        INSTANCE = this;
    }

    public static DeathStats getInstance() {
        return INSTANCE;
    }

    // Handle start, leave and deaths

    public void startSession() {
        try {
            store = MVStore.open(deathsFile.getAbsolutePath());

            // Basic key value map
            map = store.openMap("minecraft_deaths"); // FIXED
            // allow player to clear and define what a current session is.
            Integer current = (Integer)map.putIfAbsent(KEY_CURRENT, 0);
            if (current == null) {
                current = 0;
            }

            Integer max = (Integer)map.putIfAbsent(KEY_MAX, 0);
            if (max == null) {
                max = 0;
            }

            Boolean visible = (Boolean) map.putIfAbsent(KEY_IS_VISIBLE, true);
            if (visible == null) {
                visible = true;
            }

            isHighScore = false;

            LOGGER.debug("startSession {} {}", Minecraft.getInstance().player, Minecraft.getInstance().level);
            LOGGER.info("deathstats max={}, current={} visible={}", max, current, visible);
        } catch (MVStoreException mvStoreException) {
            LOGGER.error("Cannot open {}", deathsFile.getAbsolutePath(), mvStoreException);
            MutableComponent c = Component.literal("ERROR: Cannot open " + deathsFile.getAbsolutePath());
            Minecraft.getInstance().player.displayClientMessage(c, true);
        }
    }

    public void endSession() {
        LOGGER.debug("endSession");
        if (store != null) {
            store.close();
        }

        store = null;
        map = null;
    }

    public int getMax() {
        if (map == null) return -1;
        return (Integer) map.get(KEY_MAX);
    }

    public int getCurrent() {
        if (map == null) return - 1;
        return (Integer) map.get(KEY_CURRENT);
    }

    public void setMax(final int max) {
        if (map == null) return;
        map.put(KEY_MAX, max);
    }

    public boolean isVisible() {
        if (map == null) return true;
        return (Boolean) map.get(KEY_IS_VISIBLE);
    }

    public void setVisible(boolean visible) {
        if (map == null) return;
        map.put(KEY_IS_VISIBLE, visible);
    }


    public void setCurrent(final int current) {
        if (map == null) return;

        map.put(KEY_CURRENT, current);

        Integer max = (Integer) map.get(KEY_MAX);

        if (current > max) {
            map.put(KEY_MAX, current);

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
        NeoForge.EVENT_BUS.post(NewHighScoreEvent.HIGH_SCORE_EVENT);
    }

    public void playHighScoreSound() {
        LOGGER.debug("playHishScoreSound {} {}", Thread.currentThread().getName(), Thread.currentThread().getId());

        SoundEvent s = BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.fromNamespaceAndPath(MOD_ID, "high_score"));
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
        startSession();
    }

    @SubscribeEvent
    public void onLeave(ClientPlayerNetworkEvent.LoggingOut event) {
        endSession();
    }

    @SubscribeEvent
    public void onRespawn(final ClientPlayerNetworkEvent.Clone event) {
        if (event.getPlayer() == Minecraft.getInstance().player) {
            final LocalPlayer lp = event.getOldPlayer();
            if (lp.getRemovalReason() == Entity.RemovalReason.KILLED) {

                if (map == null) return;

                // update deaths
                Integer current = (Integer) map.get(KEY_CURRENT);
                current += 1;

                LOGGER.debug("death current={}", current);

                setCurrent(current);
            }
        }
    }
}
