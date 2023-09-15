package de.wightman.minecraft.deathstats.event;

import de.wightman.minecraft.deathstats.DeathStats;
import de.wightman.minecraft.deathstats.gui.ChartScreen;
import de.wightman.minecraft.deathstats.gui.KeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

public class ClientEvents {

    @Mod.EventBusSubscriber(modid = DeathStats.MOD_ID, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void onKeyPressed(InputEvent.Key event)
        {
            //if (Minecraft.getInstance().player == null) return;

            if (event.getKey() == KeyBinding.DEATH_STATS_KEY.getKey().getValue() &&  event.getAction() == GLFW.GLFW_PRESS) {
                if (!(Minecraft.getInstance().screen instanceof ChartScreen)) {
                    Minecraft.getInstance().setScreen(new ChartScreen());
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid = DeathStats.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
            event.register(KeyBinding.DEATH_STATS_KEY);
        }
    }
}
