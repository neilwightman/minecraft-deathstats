package de.wightman.minecraft.deathstats;

import de.wightman.minecraft.deathstats.event.NewHighScoreEvent;
import de.wightman.minecraft.deathstats.gui.DeathSoundEvents;
import de.wightman.minecraft.deathstats.gui.HudClientEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.MVStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;

@Mod("deathstats")
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


    public DeathStats() {
        //Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, ()->Pair.of(
                                ()->"anything. i don't care", // if i'm actually on the server, this string is sent but i'm a client only mod, so it won't be
                                (remoteversionstring,networkbool)->networkbool // i accept anything from the server, by returning true if it's asking about the server
                        ));

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::init);

        INSTANCE = this;
    }

    private void init() {
        LOGGER.info("Starting DeathStats");

        String home = System.getProperty("user.home");
        deathsFile = new File(home, "minecraft_deaths.dat");

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        // Client side commands
        MinecraftForge.EVENT_BUS.register(new DeathStatsClientCommands());
        // Hud gui
        MinecraftForge.EVENT_BUS.register(HudClientEvent.class);

        DeathSoundEvents.registerSoundEvent(eventBus);
    }

    public static DeathStats getInstance() {
        return INSTANCE;
    }

    // Handle start, leave and deaths

    public void startSession() {
        try {
            store = MVStore.open(deathsFile.getAbsolutePath());

            map = store.openMap("minecraft_deaths");
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
            TextComponent m = new StringTextComponent("ERROR: Cannot open " + deathsFile.getAbsolutePath());
            Minecraft.getInstance().player.sendMessage(m, Util.NIL_UUID);
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
        MinecraftForge.EVENT_BUS.post(NewHighScoreEvent.HIGH_SCORE_EVENT);
    }

    public void playHighScoreSound() {
        LOGGER.debug("playHishScoreSound {} {}", Thread.currentThread().getName(), Thread.currentThread().getId());

        SoundEvent s = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(MOD_ID, "high_score"));
        if (s == null) {
            LOGGER.error("high_score sound is null");
            return;
        }

        SimpleSound ssi = SimpleSound.forUI(s, 1.0f);

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
    public void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        startSession();
    }

    @SubscribeEvent
    public void onLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        endSession();
    }

    @SubscribeEvent
    public void onRespawn(PlayerEvent.Clone event) {
        if (Minecraft.getInstance().player == null) return;

        if (event.isWasDeath() && event.getOriginal().getUUID().equals(Minecraft.getInstance().player.getUUID())) {
            if (map == null) return;

            // update deaths
            Integer current = (Integer) map.get(KEY_CURRENT);
            current += 1;

            LOGGER.debug("death current={}", current);

            setCurrent(current);
        }
    }
}
