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

/**
 *
 * @author nwightma
 */
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
        TextComponent m = new TextComponent("no help, yet");
        source.getEntity().sendMessage(m, Util.NIL_UUID);
        return 0;
    }

    private static int get_all(final CommandSourceStack source) {
        get_current(source);
        get_max(source);
        return 0;
    }

    private static int get_current(final CommandSourceStack source) {
        TextComponent m = new TextComponent("Current Deaths: " + DeathStats.getInstance().getCurrent());
        source.getEntity().sendMessage(m, Util.NIL_UUID);
        return 0;
    }

    private static int get_max(final CommandSourceStack source) {
        TextComponent m = new TextComponent("Max Deaths: " + DeathStats.getInstance().getMax());
        source.getEntity().sendMessage(m, Util.NIL_UUID);
        return 0;
    }
}
