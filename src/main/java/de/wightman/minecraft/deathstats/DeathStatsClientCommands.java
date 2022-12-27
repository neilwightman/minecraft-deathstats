package de.wightman.minecraft.deathstats;

import static de.wightman.minecraft.deathstats.DeathStats.DEATH_COUNTER;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

/**
 *
 * @author nwightma
 */
public class DeathStatsClientCommands {

    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public void init(final RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("deathstats")
                .then(Commands.literal("count")
                        .executes(ctx -> count(ctx.getSource()))
                )
                .then(Commands.literal("start")
                        .executes(ctx -> start(ctx.getSource()))
                )
                .then(Commands.literal("stop")
                        .executes(ctx -> stop(ctx.getSource()))
                )
                .then(Commands.literal("help")
                        .executes(ctx -> help(ctx.getSource()))
                )
                .then(Commands.literal("debug")
                        .then(Commands.literal("info")
                                .executes(ctx -> debug(ctx.getSource()))
                        )
                )
        );
    }

    private static int count(final CommandSourceStack source) {
        final TextComponent m = new TextComponent("Deaths = " + DEATH_COUNTER.get());
        source.getEntity().sendMessage(m, Util.NIL_UUID);
        return 0;
    }

    private static int start(final CommandSourceStack source) {
        return 0;
    }

    private static int stop(final CommandSourceStack source) {
        return 0;
    }

    private static int help(final CommandSourceStack source) {
        TextComponent m = new TextComponent("help");
        source.getEntity().sendMessage(m, Util.NIL_UUID);
        return 0;
    }

    private static int debug(final CommandSourceStack source) {
        return 0;
    }
}
