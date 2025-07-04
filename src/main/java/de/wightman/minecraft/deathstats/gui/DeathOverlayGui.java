package de.wightman.minecraft.deathstats.gui;

import de.wightman.minecraft.deathstats.DeathStats;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;

import static de.wightman.minecraft.deathstats.DeathStats.MOD_ID;

public class DeathOverlayGui implements GuiLayer {

    private static final DeathStats stats = DeathStats.getInstance();

    // COLORS
    public static final int WHITE  = ARGB.color(255, 255, 255, 255);
    public static final int RED    = ARGB.color(255, 255, 0, 0);
    public static final int ORANGE = ARGB.color(255, 255, 69, 0);
    public static final int GREEN  = ARGB.color(255, 50, 205, 50);
    public static final int YELLOW = ARGB.color(255, 255, 255, 51);

    public static void register(final RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(MOD_ID, "deathstats_hud"), new DeathOverlayGui());
    }

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
                gui.drawString(font, title, width - totalWidth - 10, (height / 2) - font.lineHeight * 2, WHITE);
                gui.drawString(font, highScore, width - totalWidth - 10, (height / 2) - font.lineHeight, GREEN);
            } else {
                gui.drawString(font, title, width - totalWidth - 10, (height / 2) - font.lineHeight, WHITE);
            }

            // 50% = yellow
            // 75% = orange
            // 90% = red
            double percent = (double) current / (double) max;

            int color = WHITE;
            if (stats.isHighScore()) color = GREEN;
            else if (percent >= 0.90) color = RED;
            else if (percent >= 0.75) color = ORANGE;
            else if (percent >= 0.50) color = YELLOW;

            // current
            gui.drawString(font, line1Left, width - totalWidth - 10, (height / 2), color);
            gui.drawString(font, line1Right, width - rightWidth - 10, (height / 2), color);

            // Max
            gui.drawString(font, line2Left, width - totalWidth - 10, (height / 2) + font.lineHeight, color);
            gui.drawString(font, line2Right, width - rightWidth - 10, (height / 2) + font.lineHeight, color);
        }
    }

    public Font getFont() {
        return Minecraft.getInstance().font;
    }
}
