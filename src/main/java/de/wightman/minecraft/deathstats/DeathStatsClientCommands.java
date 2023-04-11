package de.wightman.minecraft.deathstats;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeathStatsClientCommands {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeathStatsClientCommands.class);

    @SubscribeEvent
    public void init(final RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("deathstats")
                .then(Commands.literal("get")
                        .then(Commands.literal("current")
                            .executes(ctx -> get_current(ctx.getSource()))
                        )
                        .then(Commands.literal("max")
                            .executes(ctx -> get_max(ctx.getSource()))
                        )
                        .executes(ctx -> get_all(ctx.getSource()))
                )
                .then(Commands.literal("set")
                        .then(Commands.literal("current")
                                .then(Commands.argument("current_value", StringArgumentType.string())
                                    .executes(ctx -> set_current(ctx.getSource(), StringArgumentType.getString(ctx, "current_value")))
                            )
                        )
                        .then(Commands.literal("max").then(Commands.argument("max_value", StringArgumentType.string())
                                .executes(ctx -> set_max(ctx.getSource(), StringArgumentType.getString(ctx, "max_value")))
                            )
                        )
                )
                .then(Commands.literal("help")
                        .executes(ctx -> help(ctx.getSource()))
                ).then(Commands.literal("debug")
                        .executes(ctx -> debug(ctx.getSource()))
                )
        );
    }

    private static int set_max(final CommandSourceStack source, String max) {
        DeathStats.getInstance().setMax(Integer.parseInt(max));
        return 1;
    }

    private static int set_current(final CommandSourceStack source, String current) {
        DeathStats.getInstance().setCurrent(Integer.parseInt(current));
        return 1;
    }

    private static int help(final CommandSourceStack source) {
        MutableComponent m = Component.literal("""
                DeathStats by mnkybrdr
                
                /deathstats set current <value> - set current value
                /deathstats get current - get current value
                /deathstats set max <value> - set max value
                /deathstats get max - get max value
                /deathstats debug - shows file location
                """);
        source.getEntity().sendSystemMessage(m);
        return 0;
    }

    private static int get_all(final CommandSourceStack source) {
        get_current(source);
        get_max(source);
        return 0;
    }

    private static int get_current(final CommandSourceStack source) {
        MutableComponent m = Component.literal(String.valueOf(DeathStats.getInstance().getCurrent()));
        source.getEntity().sendSystemMessage(m);
        return 0;
    }

    private static int get_max(final CommandSourceStack source) {
        MutableComponent m = Component.literal(String.valueOf(DeathStats.getInstance().getMax()));
        source.getEntity().sendSystemMessage(m);
        return 0;
    }

    private static int debug(final CommandSourceStack source) {
        MutableComponent m = Component.literal(String.valueOf(DeathStats.getInstance().getDeathsFile()));
        source.getEntity().sendSystemMessage(m);
        return 0;
    }
}
