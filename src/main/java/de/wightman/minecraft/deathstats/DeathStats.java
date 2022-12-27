package de.wightman.minecraft.deathstats;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import de.wightman.minecraft.deathstats.gui.ChartScreen;
import de.wightman.minecraft.deathstats.gui.ConfigScreen;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

@Mod("deathstats")
public class DeathStats {

    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

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
        final Config config = new Config(configBuilder);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, configBuilder.build());

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        // Server side tracking.
        MinecraftForge.EVENT_BUS.register(new DeathListener());
        // Client side commands
        MinecraftForge.EVENT_BUS.register(new DeathStatsClientCommands());

        // Register the configuration GUI factory
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiHandler.ConfigGuiFactory(new BiFunction<Minecraft, Screen, Screen>() {
                    @Override
                    public Screen apply(Minecraft mc, Screen screen) {
                        return new ConfigScreen(screen);
                    }
                }
                )
        );
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        DeathStatsServerCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onKeyPressed(InputEvent.KeyInputEvent event)
    {
        //if (Minecraft.getInstance().player == null) return;

        if (event.getKey() == key.getKey().getValue() &&
                event.getAction() == GLFW.GLFW_PRESS) {
            if (!(Minecraft.getInstance().screen instanceof ChartScreen)) {
                Minecraft.getInstance().setScreen(new ChartScreen());
            }
        }
    }

    public class Config {

        public final ForgeConfigSpec.IntValue points5s;
        public final ForgeConfigSpec.IntValue points1m;
        public final ForgeConfigSpec.IntValue points1h;

        public Config(ForgeConfigSpec.Builder builder) {
            builder.comment("DEATH STATS").push("general");

            points5s = builder.comment("Number of data points to keep at 5 seconds granularity")
                    .translation("config.deathstats.prop.5s.desc")
                    .defineInRange("5sPoints", 12, 0, Integer.MAX_VALUE);
            points1m = builder.comment("Number of data points to keep at 1 minute granularity")
                    .translation("config.deathstats.prop.1m.desc")
                    .defineInRange("1mPoints", 60, 0, Integer.MAX_VALUE);
            points1h = builder.comment("Number of data points to keep at 1 hour granularity")
                    .translation("config.deathstats.prop.1m.desc")
                    .defineInRange("1hPoints", 24, 0, Integer.MAX_VALUE);

            builder.pop();
        }

        //save() SPEC.save()
        // https://github.com/Leo3418/HBWHelper/blob/dev/src/main/java/io/github/leo3418/hbwhelper/ConfigManager.java
    }
}
