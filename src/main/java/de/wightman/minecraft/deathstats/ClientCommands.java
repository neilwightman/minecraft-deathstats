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

import java.sql.SQLException;

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
                .then(Commands.literal("set")
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
                        .then(Commands.literal("death_log")
                            .executes(ctx-> dump_death_log(ctx.getSource()))
                         )
                        .then(Commands.literal("session")
                                .executes(ctx-> dump_session(ctx.getSource()))
                        )
                        .then(Commands.literal("config")
                                .executes(ctx-> dump_config(ctx.getSource()))
                        )
                        .then(Commands.literal("sound")
                                        .executes(ctx -> sound(ctx.getSource()))
                        )
                        .executes(ctx -> debug_all(ctx.getSource()))
                ).then(Commands.literal("resume_last_session")
                    .executes( ctx -> resumeLastSession(ctx.getSource()))
                )
        );
    }

    private static int set_max(final CommandSourceStack source, String max) {
        LOGGER.info("set_max {}", max);
        try {
            DeathStats.getInstance().setMax(Long.parseLong(max));
        } catch (NumberFormatException | SQLException e) {
            MutableComponent m = Component.literal("""
            Cannot set max value,
            Error : %s
            """.formatted(e.getMessage()));
            source.getEntity().sendSystemMessage(m);
        }
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
                """);
        source.getEntity().sendSystemMessage(m);
        return 0;
    }

    private static int debug_all(final CommandSourceStack source) {
        MutableComponent m = Component.literal(String.valueOf(DeathStats.getInstance().getDbPath()));
        source.getEntity().sendSystemMessage(m);
        dump_session(source);
        dump_death_log(source);
        dump_config(source);
        return 0;
    }

    private static int sound(final CommandSourceStack source) {
        DeathStats.getInstance().playHighScoreSound();
        return 0;
    }

    private static int dump_death_log(final CommandSourceStack source) {
        DeathStats.getInstance().debugDeathLogTable();
        return 0;
    }

    private static int dump_session(final CommandSourceStack source) {
        DeathStats.getInstance().debugSessionTable();
        return 0;
    }

    private static int dump_config(final CommandSourceStack source) {
        DeathStats.getInstance().debugConfigTable();
        return 0;
    }

    private static int resumeLastSession(final CommandSourceStack source) {
        DeathStats.getInstance().resumeLastSession();
        return 0;
    }
}
