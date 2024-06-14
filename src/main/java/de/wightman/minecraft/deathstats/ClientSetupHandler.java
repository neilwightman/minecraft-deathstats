package de.wightman.minecraft.deathstats;

import de.wightman.minecraft.deathstats.gui.DeathOverlayGui;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import static de.wightman.minecraft.deathstats.DeathStats.MOD_ID;

public final class ClientSetupHandler {
    private ClientSetupHandler() {}

    @SubscribeEvent
    public static void registerGameOverlays(final RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(MOD_ID, "deathstats_hud"), new DeathOverlayGui());
    }
}
