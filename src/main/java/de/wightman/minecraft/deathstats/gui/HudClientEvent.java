package de.wightman.minecraft.deathstats.gui;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class HudClientEvent
{
    private static volatile DeathOverlayGui gui;

    @SubscribeEvent
    public static void onOverlayRender(RenderGameOverlayEvent event) {
        if (gui == null) {
            gui = new DeathOverlayGui(Minecraft.getInstance());
        }
        gui.render(event.getMatrixStack());
    }
}