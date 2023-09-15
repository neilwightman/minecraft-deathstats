package de.wightman.minecraft.deathstats.gui;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class KeyBinding {

    public static final KeyMapping DEATH_STATS_KEY = new KeyMapping("deathstats.key.name",
            GLFW.GLFW_KEY_KP_3,
            "deathstats.key.category");

}
