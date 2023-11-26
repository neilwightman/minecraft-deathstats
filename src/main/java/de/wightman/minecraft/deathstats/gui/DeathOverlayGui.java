package de.wightman.minecraft.deathstats.gui;

import de.wightman.minecraft.deathstats.DeathStats;
import de.wightman.minecraft.deathstats.event.OverlayUpdateEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.locale.Language;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class DeathOverlayGui extends GuiGraphics implements IGuiOverlay {

    private static final DeathStats stats = DeathStats.getInstance();

    // COLORS
    public static final String WHITE = "FFFFFF";
    public static final String RED = "FF0000";
    public static final String ORANGE = "FF4500";
    public static final String GREEN = "32CD32";
    public static final String YELLOW = "FFFF33";

    // Cache values
    private long current = 0;
    private long max = 0;
    private boolean isHighScore = false;
    private boolean init = false;

    public DeathOverlayGui(Minecraft minecraft, MultiBufferSource.BufferSource bufferSource) {
        super(minecraft, bufferSource);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void newDeath(OverlayUpdateEvent event) {
        readValues();
    }

    private void readValues() {
        // TODO chose session name
        current = stats.getCurrent();
        max = stats.getMax();

        // Max is from previous sessions, so we set it to current if its higher.
        if (current > max) {
            max = current;
        }
        isHighScore = stats.isHighScore();
    }


    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (DeathStats.getInstance().isVisible()) {
            if (init == false) {
                readValues();
                init = true;
            }
            int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();

            String title = Language.getInstance().getOrDefault("deathstats.overlay.title");
            String highScore = Language.getInstance().getOrDefault("deathstats.overlay.highscore");
            String line1Left = Language.getInstance().getOrDefault("deathstats.overlay.current");
            String line1Right = String.valueOf(current);
            String line2Left = Language.getInstance().getOrDefault("deathstats.overlay.max");
            String line2Right = String.valueOf(max);

            Font font = getFont();
            int leftWidth = Math.max(font.width(line1Left), font.width(line2Left));
            int rightWidth = Math.max(font.width(line1Right), font.width(line2Right));
            // Max width, can ignore high score as Death Counter is longer
            int totalWidth = Math.max(leftWidth + rightWidth + 5, font.width(title));

            // Title and highscore text
            if (isHighScore) {
                drawString(font, title, width - totalWidth - 10, (height / 2) - font.lineHeight * 2, Integer.parseInt(WHITE, 16));
                drawString(font, highScore, width - totalWidth - 10, (height / 2) - font.lineHeight, Integer.parseInt(GREEN, 16));
            } else {
                drawString(font, title, width - totalWidth - 10, (height / 2) - font.lineHeight, Integer.parseInt(WHITE, 16));
            }

            // 50% = yellow
            // 75% = orange
            // 90% = red
            double percent = (double) current / (double) max;

            String color = WHITE;
            if (isHighScore) color = GREEN;
            else if (percent >= 0.90) color = RED;
            else if (percent >= 0.75) color = ORANGE;
            else if (percent >= 0.50) color = YELLOW;

            // current
            drawString(font, line1Left, width - totalWidth - 10, (height / 2), Integer.parseInt(color, 16));
            drawString(font, line1Right, width - rightWidth - 10, (height / 2), Integer.parseInt(color, 16));

            // Max
            drawString(font, line2Left, width - totalWidth - 10, (height / 2) + font.lineHeight, Integer.parseInt(color, 16));
            drawString(font, line2Right, width - rightWidth - 10, (height / 2) + font.lineHeight, Integer.parseInt(color, 16));
        }
    }

    public Font getFont() {
        return Minecraft.getInstance().font;
    }
}
