package de.wightman.minecraft.deathstats.gui;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class KeyBinding {

    public static final KeyMapping DEATH_STATS_OVER_TIME_KEY = new KeyMapping("deathstats.key.overtime.key.name",
            GLFW.GLFW_KEY_KP_3,
            "deathstats.key.category");

    public static final KeyMapping DEATH_STATS_TOP_KEY = new KeyMapping("deathstats.key.top.name",
            GLFW.GLFW_KEY_KP_4,
            "deathstats.key.category");

}
