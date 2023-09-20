package de.wightman.minecraft.deathstats.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

import java.util.Objects;

public class TopDeathStatsWidget extends ObjectSelectionList<TopDeathStatsWidget.TopDeathStatWidgetEntry> {

    private final TopDeathStatsScreen parent;
    private final int listWidth;
    private long totalDeaths = 0;

    public TopDeathStatsWidget(TopDeathStatsScreen parent, int listWidth, int top, int bottom)
    {
        super(Minecraft.getInstance(), listWidth, parent.height, top, bottom, parent.getFont().lineHeight * 2 + 8);
        this.parent = parent;
        this.listWidth = listWidth;
    }

    public void refreshList() {
        this.clearEntries();

        totalDeaths = 0;

        var entries = parent.getLeaderBoard();
        for(var entry : entries) {
            TopDeathStatWidgetEntry death = new TopDeathStatWidgetEntry(entry.name, entry.deaths, entry.color);
            this.addEntry(death);
            totalDeaths += death.deaths;
        }
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics)
    {
        this.parent.renderBackground(guiGraphics);
    }

    @Override
    protected int getScrollbarPosition()
    {
        return this.listWidth;
    }

    @Override
    public int getRowWidth()
    {
        return this.listWidth;
    }

    public class TopDeathStatWidgetEntry extends ObjectSelectionList.Entry<TopDeathStatWidgetEntry>{

        private final String name;
        private final long deaths;
        private final int color;

        public TopDeathStatWidgetEntry(String name, long deaths, int color) {
            Objects.requireNonNull(name);
            this.name = name;
            this.deaths = deaths;
            this.color = color;
        }

        @Override
        public Component getNarration() {
            return null;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            Component nc = Component.literal(name);
            Component val = Component.literal(String.valueOf(deaths));

            int c = color;
            if (c == 0) {
                c = ChatFormatting.GRAY.getColor();
            }

            float percentage = (float)deaths / (float)totalDeaths;
            float width = (left + entryWidth) * percentage;
            guiGraphics.fill(left + 5, top + (entryHeight/2) +3, (int)width, top + entryHeight -2 , c | (0xFF << 24));

            guiGraphics.drawString(parent.getFont(), Language.getInstance().getVisualOrder(FormattedText.composite(parent.getFont().substrByWidth(nc, listWidth))),
                    left + 5, top + 3, c, false);
            guiGraphics.drawString(parent.getFont(), Language.getInstance().getVisualOrder(FormattedText.composite(parent.getFont().substrByWidth(val, listWidth))),
                    entryWidth -50, top + 3, c, false);

            guiGraphics.hLine( left, left + entryWidth, top + entryHeight, 584700626);

            if (isMouseOver) {
                parent.setTooltipForNextRenderPass(Component.translatable("deathstats.top.tooltip", name, deaths));
            }
        }
    }
}
