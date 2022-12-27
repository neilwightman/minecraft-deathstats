package de.wightman.minecraft.deathstats.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

import javax.annotation.Nullable;

public class ChartComponent extends GuiComponent {

    public void render(final PoseStack poseStack, final int height, final int width) {
        fill( poseStack, 50 , 50, width - 50 , height - 50, 16711680);
    }
}
