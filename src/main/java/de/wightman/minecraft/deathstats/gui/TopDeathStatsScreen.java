package de.wightman.minecraft.deathstats.gui;

import de.wightman.minecraft.deathstats.DeathRecord;
import de.wightman.minecraft.deathstats.DeathStats;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.forgespi.language.IModInfo;
import org.h2.mvstore.MVMap;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class TopDeathStatsScreen extends Screen {
    private final Screen parentScreen;
    private TopDeathStatsWidget deathStatsWidget;
    List<TopDeathStatsWidget.TopDeathStatEntry> list = new ArrayList<>();


    public TopDeathStatsScreen(Screen parentScreen) {
        super(Component.translatable("deathstats.top.title"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();

        this.deathStatsWidget = new TopDeathStatsWidget(this, width - 20, 20, height);
        //this.deathStatsWidget.setLeftPos(6);

        this.addRenderableWidget(deathStatsWidget);

        this.deathStatsWidget.refreshList();
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

    public <T extends ObjectSelectionList.Entry<T>> void buildList(Consumer<T> consumer, Function<TopDeathStatsWidget.TopDeathStatEntry, T> newEntry)
    {
        list.forEach(mod->consumer.accept(newEntry.apply(mod)));
    }

    public Set<Map.Entry<String,Long>> getEntries() {
        // Should be cached only called once per screen open.
        MVMap<Long, String> log = DeathStats.getInstance().getDeathLog();

        Map<String, Long> deathTypeToCountMap = new HashMap<>();

        for(String val :log.values())
        {
            DeathRecord dr = DeathRecord.fromString(val);
            String name = dr.killedByStr;
            if (name == null) name = dr.killedByKey;
            if (name == null) name = dr.deathMessage;

            Long l = deathTypeToCountMap.get(name);
            if (l == null) l = 0L;

            deathTypeToCountMap.put(name, l + 1L);
        }
        System.out.println(deathTypeToCountMap.size());

        List<TopDeathStatsWidget.TopDeathStatEntry> list = new ArrayList<>();

        return deathTypeToCountMap.entrySet();
    }

}
