package de.wightman.minecraft.deathstats;

import de.wightman.minecraft.deathstats.gui.DeathSoundEvents;
import de.wightman.minecraft.deathstats.gui.HudClientEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
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

@Mod("deathstats")
public class DeathStats {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeathStats.class);

    public static final String MOD_ID = "deathstats";

    private static DeathStats INSTANCE;

    private File deathsFile;
    private MVStore store;
    private MVMap<String, Integer> map;
    private boolean isHighScore = false;

    private static final String KEY_MAX = "session_death_max";
    private static final String KEY_CURRENT = "current_session_deaths";


    public DeathStats() {
        //Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "ANY", (remote, isServer) -> true));

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
            Integer current = map.putIfAbsent(KEY_CURRENT, 0);
            if (current == null) {
                current = 0;
            }

            Integer max = map.putIfAbsent(KEY_MAX, 0);
            if (max == null) {
                max = 0;
            }

            isHighScore = false;

            LOGGER.debug("startSession {} {}", Minecraft.getInstance().player, Minecraft.getInstance().level);
            LOGGER.debug("deaths max={}, current={}", max, current);
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
        map = null;
    }

    public int getMax() {
        if (map == null) return -1;
        return map.get(KEY_MAX);
    }

    public int getCurrent() {
        if (map == null) return - 1;
        return map.get(KEY_CURRENT);
    }

    public void setMax(final int max) {
        if (map == null) return;
        map.put(KEY_MAX, max);
    }

    public void setCurrent(final int current) {
        if (map == null) return;

        map.put(KEY_CURRENT, current);

        Integer max = map.get(KEY_MAX);

        if (current > max) {
            map.put(KEY_MAX, current);

            if (!isHighScore) {
                playHighScoreSound();
            }

            isHighScore = true;
        } else {
            isHighScore = false;
        }
    }

    public void playHighScoreSound() {
        LOGGER.info("playHishScoreSound {} {}", Thread.currentThread().getName(), Thread.currentThread().getId());

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
        startSession();
    }

    @SubscribeEvent
    public void onLeave(ClientPlayerNetworkEvent.LoggingOut event) {
        endSession();
    }

    @SubscribeEvent
    public void onRespawn(ClientPlayerNetworkEvent.Clone event) {
        if (event.getPlayer() == Minecraft.getInstance().player) {
            if (map == null) return;

            // update deaths
            Integer current = map.get(KEY_CURRENT);
            current += 1;

            LOGGER.debug("death current={}", current);

            setCurrent(current);
        }
    }
}
