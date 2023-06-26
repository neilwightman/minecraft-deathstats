package de.wightman.minecraft.deathstats;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeathStatsClientCommands {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeathStatsClientCommands.class);

    @SubscribeEvent
    public void init(final RegisterCommandsEvent event) {
        // Work around for CCI
        // https://github.com/iChun/ContentCreatorIntegration-IssuesAndDocumentation/issues/89
        // CCI runs the commands as though it was running on the server
        // WARNING : this will only work when playing with a local (embedded) minecraft server.
        // code will not load on a server as its only Dist.CLIENT
        event.getDispatcher().register(Commands.literal("deathstats")
                .then(Commands.literal("get")
                        .then(Commands.literal("current")
                                .executes(ctx -> get_current(ctx.getSource()))
                        )
                        .then(Commands.literal("max")
                                .executes(ctx -> get_max(ctx.getSource()))
                        )
                        .then(Commands.literal("highscore")
                                .executes(ctx -> get_highscore(ctx.getSource()))
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
                        ).then(Commands.literal("visible").then(Commands.argument("is_visible", StringArgumentType.string())
                                        .executes(ctx -> set_visible(ctx.getSource(), StringArgumentType.getString(ctx, "is_visible")))
                                )
                        )
                )
                .then(Commands.literal("help")
                        .executes(ctx -> help(ctx.getSource()))
                ).then(Commands.literal("debug")
                        .executes(ctx -> debug(ctx.getSource()))
                ).then(Commands.literal("sound")
                        .executes(ctx -> sound(ctx.getSource()))
                ).then(Commands.literal("reset")
                        .executes(ctx -> reset(ctx.getSource()))
                )
        );
    }

    private static int set_max(final CommandSource source, String max) {
        LOGGER.info("set_max {}", max);
        DeathStats.getInstance().setMax(Integer.parseInt(max));
        return 1;
    }

    private static int set_current(final CommandSource source, String current) {
        LOGGER.info("set_current {}", current);
        DeathStats.getInstance().setCurrent(Integer.parseInt(current));
        return 1;
    }

    private static int set_visible(final CommandSource source, String current) {
        LOGGER.info("set_visible {}", current);
        DeathStats.getInstance().setVisible(Boolean.parseBoolean(current));
        return 1;
    }

    private static int help(final CommandSource source) {
        TextComponent m = new StringTextComponent("""
                §lDeathStats§r by §6mnkybrdr§r

                §6/deathstats§f set current §2<value>§f - §oset current value§r
                §6/deathstats§f set max §2<value>§f - §oset max value§r
                §6/deathstats§f set visible §2<true|value>§f - §ohides or shows the overlay§r
                §6/deathstats§f get current - §oget current value§r
                §6/deathstats§f get max - §oget max value§r
                §6/deathstats§f get highscore - §ohas the highscore hit§r
                §6/deathstats§f debug - §oshows debug information§r
                §6/deathstats§f sound - §oplays high score sound§r
                §6/deathstats§f reset - §osets max and current to 0§r
                """);
        source.getEntity().sendMessage(m, Util.NIL_UUID);
        return 0;
    }

    private static int get_all(final CommandSource source) {
        int current = DeathStats.getInstance().getCurrent();
        int max = DeathStats.getInstance().getMax();
        boolean highScore = DeathStats.getInstance().isHighScore();
        boolean visible = DeathStats.getInstance().isVisible();
        TextComponent m = new StringTextComponent("""
                {
                     §4current§f: §9%s§f,
                     §4max§f: §9%s§f,
                     §4highScore§f: §2%s§f
                     §4visible§f: §2%s§f
                }
                """.formatted(current, max, highScore, visible));
        source.getEntity().sendMessage(m, Util.NIL_UUID);
        return 0;
    }

    private static int get_current(final CommandSource source) {
        TextComponent m = new StringTextComponent(String.valueOf(DeathStats.getInstance().getCurrent()));
        source.getEntity().sendMessage(m, Util.NIL_UUID);
        return 0;
    }

    private static int get_max(final CommandSource source) {
        TextComponent m = new StringTextComponent(String.valueOf(DeathStats.getInstance().getMax()));
        source.getEntity().sendMessage(m, Util.NIL_UUID);
        return 0;
    }

    private static int get_highscore(final CommandSource source) {
        TextComponent m = new StringTextComponent(String.valueOf(DeathStats.getInstance().isHighScore()));
        source.getEntity().sendMessage(m, Util.NIL_UUID);
        return 0;
    }

    private static int debug(final CommandSource source) {
        TextComponent m = new StringTextComponent(String.valueOf(DeathStats.getInstance().getDeathsFile()));
        source.getEntity().sendMessage(m, Util.NIL_UUID);
        get_all(source);
        return 0;
    }

    private static int reset(final CommandSource source) {
        set_current(source, "0");
        set_max(source, "0");
        return 0;
    }

    private static int sound(final CommandSource source) {
        DeathStats.getInstance().playHighScoreSound();
        return 0;
    }
}
