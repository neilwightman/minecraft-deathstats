package de.wightman.minecraft.deathstats;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 *
 * @author nwightma
 */
public class DeathStatsServerCommands {

    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("deathstats_srv")
                .then(Commands.literal("count")
                        .executes(ctx -> count(ctx.getSource()))
                )
                .then(Commands.literal("start")
                        .executes(ctx -> start(ctx.getSource()))
                )
                .then(Commands.literal("stop")
                        .executes(ctx -> stop(ctx.getSource()))
                )
        );
    }

    private static int count(CommandSourceStack source) {
        return 1;
    }

    private static int start(CommandSourceStack source) {
        return 1;
    }

    private static int stop(CommandSourceStack source) {
        return 1;
    }
}
