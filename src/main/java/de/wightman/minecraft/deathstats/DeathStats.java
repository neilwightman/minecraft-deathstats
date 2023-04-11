package de.wightman.minecraft.deathstats;

import de.wightman.minecraft.deathstats.gui.HudClientEvent;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
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

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        // Client side commands
        MinecraftForge.EVENT_BUS.register(new DeathStatsClientCommands());
        // Hud gui
        MinecraftForge.EVENT_BUS.register(HudClientEvent.class);
    }

    public static DeathStats getInstance() {
        return INSTANCE;
    }

    public void handlePlayerCombatKill(final ClientboundPlayerCombatKillPacket clientboundPlayerCombatKillPacket) {
        final ClientLevel level = Minecraft.getInstance().level;
        final Entity entity = level.getEntity(clientboundPlayerCombatKillPacket.getPlayerId());
        // Only process LocalPlayer
        if (entity == Minecraft.getInstance().player) {
            if (map == null) return;

            // update deaths
            Integer current = map.get(KEY_CURRENT);
            current += 1;

            LOGGER.debug("death current={}", current);

            setCurrent(current);
        }
    }

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
            TextComponent m = new TextComponent("ERROR: Cannot open " + deathsFile.getAbsolutePath());
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

            isHighScore = true;
        } else {
            isHighScore = false;
        }
    }

    public boolean isHighScore() {
        return isHighScore;
    }
}
