package de.wightman.minecraft.deathstats;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import de.wightman.minecraft.deathstats.gui.ChartScreen;

import java.util.concurrent.atomic.AtomicLong;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.LoggerFactory;

@Mod("deathstats")
public class DeathStats {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeathStats.class);

    public static final String MOD_ID = "deathstats";

    public static final AtomicLong DEATH_COUNTER = new AtomicLong(0L);

    private static final KeyMapping key = new KeyMapping("deathstats.key.name", InputConstants.KEY_NUMPAD3, "deathstats.key.category");

    public DeathStats() {
        //Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "ANY", (remote, isServer) -> true));

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::init);

        ClientRegistry.registerKeyBinding(key);
    }

    private void init() {
        LOGGER.info("Starting DeathStats");

        final ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        final DeathStatsConfig config = new DeathStatsConfig(configBuilder);
        final ForgeConfigSpec configSpec = configBuilder.build();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, configSpec);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        // Client side commands
        MinecraftForge.EVENT_BUS.register(new DeathStatsClientCommands());
    }

    @SubscribeEvent
    public void onKeyPressed(InputEvent.KeyInputEvent event)
    {
        if (Minecraft.getInstance().player == null) return;

        if (event.getKey() == key.getKey().getValue() &&  event.getAction() == GLFW.GLFW_PRESS) {
            if (!(Minecraft.getInstance().screen instanceof ChartScreen)) {
                Minecraft.getInstance().setScreen(new ChartScreen());
            }
        }
    }
}
