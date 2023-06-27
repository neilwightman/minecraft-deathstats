package de.wightman.minecraft.deathstats.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import de.wightman.minecraft.deathstats.DeathStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.OverlayRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.lwjgl.opengl.GL11;

public class DeathOverlayGui extends AbstractGui {

    private static final DeathStats stats = DeathStats.getInstance();

    // COLORS
    public static final String WHITE = "FFFFFF";
    public static final String RED = "FF0000";
    public static final String ORANGE = "FF4500";
    public static final String GREEN = "32CD32";
    public static final String YELLOW = "FFFF33";
    private final Minecraft minecraft;

    public DeathOverlayGui(Minecraft minecraft) {
        super();
        this.minecraft = minecraft;
    }

    public FontRenderer getFont() {
        return this.minecraft.font;
    }

    public void drawContents(MatrixStack poseStack) {
        if (DeathStats.getInstance().isVisible()) {
            int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();

            int current = stats.getCurrent();
            int max = stats.getMax();

            String title = "§lDeath Counter§r";
            String highScore = "New High Score";
            String line1Left = "Current: ";
            String line1Right = String.valueOf(current);
            String line2Left = "Max: ";
            String line2Right = String.valueOf(max);

            FontRenderer font = getFont();
            int leftWidth = Math.max(font.width(line1Left), font.width(line2Left));
            int rightWidth = Math.max(font.width(line1Right), font.width(line2Right));
            // Max width, can ignore high score as Death Counter is longer
            int totalWidth = Math.max(leftWidth + rightWidth + 5, font.width(title));

            // Title and highscore text
            if (stats.isHighScore()) {
                drawString(poseStack, this.getFont(), title, width - totalWidth - 10, (height / 2) - font.lineHeight * 2, Integer.parseInt(WHITE, 16));
                drawString(poseStack, this.getFont(), highScore, width - totalWidth - 10, (height / 2) - font.lineHeight, Integer.parseInt(GREEN, 16));
            } else {
                drawString(poseStack, this.getFont(), title, width - totalWidth - 10, (height / 2) - font.lineHeight, Integer.parseInt(WHITE, 16));
            }

            // 50% = yellow
            // 75% = orange
            // 90% = red
            double percent = (double) current / (double) max;

            String color = WHITE;
            if (stats.isHighScore()) color = GREEN;
            else if (percent >= 0.90) color = RED;
            else if (percent >= 0.75) color = ORANGE;
            else if (percent >= 0.50) color = YELLOW;

            // current
            drawString(poseStack, this.getFont(), line1Left, width - totalWidth - 10, (height / 2), Integer.parseInt(color, 16));
            drawString(poseStack, this.getFont(), line1Right, width - rightWidth - 10, (height / 2), Integer.parseInt(color, 16));

            // Max
            drawString(poseStack, this.getFont(), line2Left, width - totalWidth - 10, (height / 2) + font.lineHeight, Integer.parseInt(color, 16));
            drawString(poseStack, this.getFont(), line2Right, width - rightWidth - 10, (height / 2) + font.lineHeight, Integer.parseInt(color, 16));
        }
    }
}
