package de.wightman.minecraft.deathstats;

import de.wightman.minecraft.deathstats.gui.DeathOverlayGui;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static de.wightman.minecraft.deathstats.DeathStats.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetupHandler {
    private ClientSetupHandler() {}

    @SubscribeEvent
    public static void registerGameOverlays(final RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("deathstats_hud", new DeathOverlayGui());
    }
}
