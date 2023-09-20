package de.wightman.minecraft.deathstats.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class TopDeathStatsWidget extends ObjectSelectionList<TopDeathStatsWidget.TopDeathStatEntry> {

    private final TopDeathStatsScreen parent;
    private final int listWidth;

    public TopDeathStatsWidget(TopDeathStatsScreen parent, int listWidth, int top, int bottom)
    {
        super(Minecraft.getInstance(), listWidth, parent.height, top, bottom, parent.getFont().lineHeight * 2 + 8);
        this.parent = parent;
        this.listWidth = listWidth;
    }

    public void refreshList() {
        this.clearEntries();

        Set<Map.Entry<String, Long>> entries = parent.getEntries();
        for(var entry : entries) {
            this.addEntry(new TopDeathStatEntry(entry.getKey(), entry.getValue()));
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

    public class TopDeathStatEntry extends ObjectSelectionList.Entry<TopDeathStatEntry>{

        private final String name;
        private final long value;

        public TopDeathStatEntry(String name, long value) {
            Objects.requireNonNull(name);
            this.name = name;
            this.value = value;
        }

        @Override
        public Component getNarration() {
            return null;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            Component nc = Component.literal(name);
            Component val = Component.literal(String.valueOf(value));
            guiGraphics.drawString(parent.getFont(), Language.getInstance().getVisualOrder(FormattedText.composite(parent.getFont().substrByWidth(nc, listWidth))), left + 3, top + 2, 0xFFFFFF, false);
            guiGraphics.drawString(parent.getFont(), Language.getInstance().getVisualOrder(FormattedText.composite(parent.getFont().substrByWidth(val, listWidth))), entryWidth -50, top + 2, 0xFFFFFF, false);
        }
    }
}
