package de.wightman.minecraft.deathstats.gui;

import de.wightman.minecraft.deathstats.DeathRecord;
import de.wightman.minecraft.deathstats.DeathStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import org.h2.mvstore.MVMap;

import java.util.*;

public class TopDeathStatsScreen extends Screen {
    private final Screen parentScreen;
    private TopDeathStatsWidget deathStatsWidget;
    private final List<DeathLeaderBoardEntry> list = new ArrayList<>();
    private static final DeathLeaderBoardComparator cmp = new DeathLeaderBoardComparator();

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
        // Should be cached only called once per screen open.
        MVMap<Long, String> log = DeathStats.getInstance().getDeathLog();

        String player = Minecraft.getInstance().player.getName().getString();

        Map<String, Long> deathTypeToCountMap = new HashMap<>();
        Map<String, Integer> colors = new HashMap<>();

        for(String val :log.values())
        {
            // probably best to store this info on death
            DeathRecord dr = DeathRecord.fromString(val);
            String name = dr.killedByStr;
            if (name == null && dr.killedByKey != null) {
                name = Component.translatable(dr.killedByKey).getString();
            }
            if (name == null) {
                name = Component.translatable(dr.deathMessage, player).getString();
            }

            Long l = deathTypeToCountMap.get(name);
            if (l == null) l = 0L;

            deathTypeToCountMap.put(name, l + 1L);

            colors.put(name, dr.argb);
        }

        for (var entry : deathTypeToCountMap.entrySet()) {
            String name = entry.getKey();
            int color = colors.get(name);
            list.add(new DeathLeaderBoardEntry(name, entry.getValue(), color));
        }

        list.sort(cmp);
    }

    public static class DeathLeaderBoardEntry {
        public final String name;
        public final long deaths;
        public final int color;

        public DeathLeaderBoardEntry(String name, long deaths, int color) {
            this.name = name;
            this.deaths = deaths;
            this.color = color;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DeathLeaderBoardEntry that = (DeathLeaderBoardEntry) o;
            return deaths == that.deaths && color == that.color && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, deaths, color);
        }
    }

    public static class DeathLeaderBoardComparator implements Comparator<DeathLeaderBoardEntry> {

        @Override
        public int compare(DeathLeaderBoardEntry o1, DeathLeaderBoardEntry o2) {
            return (int)(o2.deaths - o1.deaths);
        }
    }
}
