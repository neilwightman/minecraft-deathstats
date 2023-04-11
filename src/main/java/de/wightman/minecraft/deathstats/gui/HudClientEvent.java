package de.wightman.minecraft.deathstats.gui;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class HudClientEvent
{
    private static volatile DeathOverlayGui gui;

    @SubscribeEvent
    public static void onOverlayRender(RenderGuiOverlayEvent event) {
        if (gui == null) {
            gui = new DeathOverlayGui(Minecraft.getInstance(), Minecraft.getInstance().getItemRenderer());
        }
        gui.render(event.getPoseStack());
    }
}