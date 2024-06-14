package de.wightman.minecraft.deathstats.gui;

import de.wightman.minecraft.deathstats.DeathStats;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;

public class DeathOverlayGui implements LayeredDraw.Layer {

    private static final DeathStats stats = DeathStats.getInstance();

    // COLORS
    public static final String WHITE = "FFFFFF";
    public static final String RED = "FF0000";
    public static final String ORANGE = "FF4500";
    public static final String GREEN = "32CD32";
    public static final String YELLOW = "FFFF33";

    @Override
    public void render(GuiGraphics gui, DeltaTracker tracker) {
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

            Font font = getFont();
            int leftWidth = Math.max(font.width(line1Left), font.width(line2Left));
            int rightWidth = Math.max(font.width(line1Right), font.width(line2Right));
            // Max width, can ignore high score as Death Counter is longer
            int totalWidth = Math.max(leftWidth + rightWidth + 5, font.width(title));

            // Title and highscore text
            if (stats.isHighScore()) {
                gui.drawString(font, title, width - totalWidth - 10, (height / 2) - font.lineHeight * 2, Integer.parseInt(WHITE, 16));
                gui.drawString(font, highScore, width - totalWidth - 10, (height / 2) - font.lineHeight, Integer.parseInt(GREEN, 16));
            } else {
                gui.drawString(font, title, width - totalWidth - 10, (height / 2) - font.lineHeight, Integer.parseInt(WHITE, 16));
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
            gui.drawString(font, line1Left, width - totalWidth - 10, (height / 2), Integer.parseInt(color, 16));
            gui.drawString(font, line1Right, width - rightWidth - 10, (height / 2), Integer.parseInt(color, 16));

            // Max
            gui.drawString(font, line2Left, width - totalWidth - 10, (height / 2) + font.lineHeight, Integer.parseInt(color, 16));
            gui.drawString(font, line2Right, width - rightWidth - 10, (height / 2) + font.lineHeight, Integer.parseInt(color, 16));
        }
    }

    public Font getFont() {
        return Minecraft.getInstance().font;
    }
}
