package de.wightman.minecraft.deathstats;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * New config UI coming so I removed the current custom incomplete UI.
 * @see https://github.com/MinecraftForge/MinecraftForge/pull/8874
 **/
public class DeathStatsConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeathStatsConfig.class);
    private static DeathStatsConfig INSTANCE = null;

    private final ForgeConfigSpec.IntValue points5s;
    private final ForgeConfigSpec.IntValue points1m;
    private final ForgeConfigSpec.IntValue points1h;

    private ForgeConfigSpec configSpec;

    public DeathStatsConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Death Stats General Settings").push("general");

        points5s = builder.comment("Number of data points to keep at 5 seconds granularity")
                .translation("deathstats.config.prop.5s.desc")
                .defineInRange("5sPoints", 12, 0, Integer.MAX_VALUE);
        points1m = builder.comment("Number of data points to keep at 1 minute granularity")
                .translation("deathstats.config.1m.desc")
                .defineInRange("1mPoints", 60, 0, Integer.MAX_VALUE);
        points1h = builder.comment("Number of data points to keep at 1 hour granularity")
                .translation("deathstats.config.1h.desc")
                .defineInRange("1hPoints", 24, 0, Integer.MAX_VALUE);

        builder.pop();

        INSTANCE = this;
    }

    public DeathStatsConfig getInstance() {
        return INSTANCE;
    }

    public void save() {
        configSpec.save();
    }

    public void setSpec(ForgeConfigSpec configSpec) {
        this.configSpec = configSpec;
    }
}
