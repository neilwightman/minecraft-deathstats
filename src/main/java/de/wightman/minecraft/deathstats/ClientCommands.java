package de.wightman.minecraft.deathstats;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.wightman.minecraft.deathstats.DeathStats.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public final class ClientCommands {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientCommands.class);

    @SubscribeEvent
    public static void init(final RegisterCommandsEvent event) {
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

    private static int set_max(final CommandSourceStack source, String max) {
        LOGGER.info("set_max {}", max);
        DeathStats.getInstance().setMax(Integer.parseInt(max));
        return 1;
    }

    private static int set_current(final CommandSourceStack source, String current) {
        LOGGER.info("set_current {}", current);
        DeathStats.getInstance().setCurrent(Integer.parseInt(current));
        return 1;
    }

    private static int set_visible(final CommandSourceStack source, String current) {
        LOGGER.info("set_visible {}", current);
        DeathStats.getInstance().setVisible(Boolean.parseBoolean(current));
        return 1;
    }

    private static int help(final CommandSourceStack source) {
        MutableComponent m = Component.literal("""
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
        source.getEntity().sendSystemMessage(m);
        return 0;
    }

    private static int get_all(final CommandSourceStack source) {
        int current = DeathStats.getInstance().getCurrent();
        int max = DeathStats.getInstance().getMax();
        boolean highScore = DeathStats.getInstance().isHighScore();
        boolean visible = DeathStats.getInstance().isVisible();
        MutableComponent m = Component.literal("""
                {
                     §4current§f: §9%s§f,
                     §4max§f: §9%s§f,
                     §4highScore§f: §2%s§f
                     §4visible§f: §2%s§f
                }
                """.formatted(current, max, highScore, visible));
        source.getEntity().sendSystemMessage(m);
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

    private static int get_highscore(final CommandSourceStack source) {
        MutableComponent m = Component.literal(String.valueOf(DeathStats.getInstance().isHighScore()));
        source.getEntity().sendSystemMessage(m);
        return 0;
    }

    private static int debug(final CommandSourceStack source) {
        MutableComponent m = Component.literal(String.valueOf(DeathStats.getInstance().getDeathsFile()));
        source.getEntity().sendSystemMessage(m);
        get_all(source);
        return 0;
    }

    private static int reset(final CommandSourceStack source) {
        set_current(source, "0");
        set_max(source, "0");
        return 0;
    }

    private static int sound(final CommandSourceStack source) {
        DeathStats.getInstance().playHighScoreSound();
        return 0;
    }
}
