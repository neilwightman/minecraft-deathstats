package de.wightman.minecraft.deathstats.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.wightman.minecraft.deathstats.DeathStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;

public class DeathOverlayGui extends Gui {

    private static final DeathStats stats = DeathStats.getInstance();

    public DeathOverlayGui(Minecraft minecraft, ItemRenderer itemRenderer) {
        super(minecraft, itemRenderer);
    }

    public void render(PoseStack poseStack) {
        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        String line1 = "Current Deaths: " + stats.getCurrent();
        String line2 = "Max Deaths: " + stats.getMax();

        Font f = getFont();
        int fontWidth = Math.max(f.width(line1), f.width(line2));

        int current = stats.getCurrent();
        int max = stats.getMax();
        if (stats.isHighScore()) {
            drawString(poseStack, this.getFont(), "High Score", width - fontWidth - 10 , (height / 2) -  f.lineHeight, Integer.parseInt("00FF00", 16));
        }
        drawString(poseStack, this.getFont(), line1, width - fontWidth - 10 , (height / 2) , Integer.parseInt("FFFFFF", 16));
        drawString(poseStack, this.getFont(), line2, width - fontWidth - 10 , (height / 2) + f.lineHeight, Integer.parseInt("FFFFFF", 16));
    }
}
