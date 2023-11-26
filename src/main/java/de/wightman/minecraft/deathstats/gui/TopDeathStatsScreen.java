package de.wightman.minecraft.deathstats.gui;

import de.wightman.minecraft.deathstats.DeathStats;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.*;

public class TopDeathStatsScreen extends Screen {
    private final Screen parentScreen;
    private TopDeathStatsWidget deathStatsWidget;
    private List<DeathLeaderBoardEntry> list = Collections.emptyList();

    public TopDeathStatsScreen(Screen parentScreen) {
        super(Component.translatable("deathstats.top.title"));
        this.parentScreen = parentScreen;
        buildEntries();
    }

    @Override
    protected void init() {
        super.init();

        this.deathStatsWidget = new TopDeathStatsWidget(this, width - 8, 20, height - 20);

        this.addRenderableWidget(deathStatsWidget);

        this.deathStatsWidget.refreshList();
    }

    @Override
    public void setTooltipForNextRenderPass(Component p_259986_) {
        super.setTooltipForNextRenderPass(p_259986_);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.deathStatsWidget.render(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(width / 2, 5, 0);
        guiGraphics.drawCenteredString(this.font, this.title.getString(), 0, 0, 0xFFFFFF);
        guiGraphics.pose().popPose();

        // TODO Add total to bottom right.
    }

    protected Font getFont() {
        return this.font;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parentScreen);
    }

    public List<DeathLeaderBoardEntry> getLeaderBoard() {
        return list;
    }

    private void buildEntries() {
        int id = DeathStats.getInstance().getActiveSessionId();
        list = DeathStats.getInstance().getLeaderBoardForSession(id);
    }

    public static class DeathLeaderBoardEntry {
        public final long count;
        public final String message;
        public final String killedByKey;
        public final String killedByStr;
        public final int color;

        public DeathLeaderBoardEntry(long count, String message, String killedByKey, String killedByStr, int color) {
            this.count = count;
            this.message = message;
            this.killedByKey = killedByKey;
            this.killedByStr = killedByStr;
            this.color = color;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DeathLeaderBoardEntry that = (DeathLeaderBoardEntry) o;
            return count == that.count && Objects.equals(message, that.message) && Objects.equals(killedByKey, that.killedByKey) && Objects.equals(killedByStr, that.killedByStr);
        }

        @Override
        public int hashCode() {
            return Objects.hash(count, message, killedByKey, killedByStr);
        }
    }
}
