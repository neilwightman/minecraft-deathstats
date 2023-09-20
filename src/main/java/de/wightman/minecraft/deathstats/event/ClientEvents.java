package de.wightman.minecraft.deathstats.event;

import de.wightman.minecraft.deathstats.DeathStats;
import de.wightman.minecraft.deathstats.gui.DeathsOverTimeChartScreen;
import de.wightman.minecraft.deathstats.gui.KeyBinding;
import de.wightman.minecraft.deathstats.gui.TopDeathStatsScreen;
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
            if (Minecraft.getInstance().player == null) return;

            if (event.getKey() == KeyBinding.DEATH_STATS_OVER_TIME_KEY.getKey().getValue() &&  event.getAction() == GLFW.GLFW_PRESS) {
                if (!(Minecraft.getInstance().screen instanceof DeathsOverTimeChartScreen)) {
                    Minecraft.getInstance().setScreen(new DeathsOverTimeChartScreen());
                }
            }
            if (event.getKey() == KeyBinding.DEATH_STATS_TOP_KEY.getKey().getValue() &&  event.getAction() == GLFW.GLFW_PRESS) {
                if (!(Minecraft.getInstance().screen instanceof TopDeathStatsScreen)) {
                    Minecraft.getInstance().setScreen(new TopDeathStatsScreen(Minecraft.getInstance().screen));
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid = DeathStats.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
            event.register(KeyBinding.DEATH_STATS_OVER_TIME_KEY);
            event.register(KeyBinding.DEATH_STATS_TOP_KEY);
        }
    }
}
