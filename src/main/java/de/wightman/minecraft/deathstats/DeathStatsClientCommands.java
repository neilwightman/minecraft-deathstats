package de.wightman.minecraft.deathstats;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
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
        TextComponent m = new TextComponent("""
                §lDeathStats§r by §6mnkybrdr§r

                §6/deathstats§f set current §2<value>§f - §oset current value§r
                §6/deathstats§f get current - §oget current value§r
                §6/deathstats§f set max §2<value>§f - §oset max value§r
                §6/deathstats§f get max - §oget max value§r
                §6/deathstats§f debug - §oshows debug information§r
                """);
        source.getEntity().sendMessage(m, Util.NIL_UUID);
        return 0;
    }

    private static int get_all(final CommandSourceStack source) {
        int current = DeathStats.getInstance().getCurrent();
        int max = DeathStats.getInstance().getMax();
        boolean highScore = DeathStats.getInstance().isHighScore();
        TextComponent m = new TextComponent("""
                { 
                     §4current§f: §9%s§f,
                     §4max§f: §9%s§f,
                     §4highScore§f: §2%s§f
                }  
                """.formatted(current, max, highScore));
        source.getEntity().sendMessage(m, Util.NIL_UUID);
        return 0;
    }

    private static int get_current(final CommandSourceStack source) {
        TextComponent m = new TextComponent(String.valueOf(DeathStats.getInstance().getCurrent()));
        source.getEntity().sendMessage(m, Util.NIL_UUID);
        return 0;
    }

    private static int get_max(final CommandSourceStack source) {
        TextComponent m = new TextComponent(String.valueOf(DeathStats.getInstance().getMax()));
        source.getEntity().sendMessage(m, Util.NIL_UUID);
        return 0;
    }

    private static int debug(final CommandSourceStack source) {
        TextComponent m = new TextComponent(String.valueOf(DeathStats.getInstance().getDeathsFile()));
        source.getEntity().sendMessage(m, Util.NIL_UUID);
        get_all(source);
        return 0;
    }
}
