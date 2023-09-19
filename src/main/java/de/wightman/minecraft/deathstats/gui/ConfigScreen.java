package de.wightman.minecraft.deathstats.gui;

import net.minecraft.Util;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Objects;

// Use cloth?
public final class ConfigScreen extends Screen {

    /**
     * URL to the help page with more information about this mod's settings
     */
    private static final String MORE_INFO_URL = "https://www.test.com";

    /**
     * Distance between this GUI's title and the top of the screen
     */
    private static final int TITLE_HEIGHT = 8;

    /**
     * Distance between the options list's top and the top of the screen
     */
    private static final int OPTIONS_LIST_TOP_HEIGHT = 24;

    /**
     * Distance between the options list's bottom and the bottom of the screen
     */
    private static final int OPTIONS_LIST_BOTTOM_OFFSET = 32;

    /**
     * Distance between the top of each button below the options list and the bottom of the screen
     */
    private static final int BOTTOM_BUTTON_HEIGHT_OFFSET = 26;

    /**
     * Height of each item in the options list
     */
    private static final int OPTIONS_LIST_ITEM_HEIGHT = 25;

    /**
     * Width of each button below the options list
     */
    private static final int BOTTOM_BUTTON_WIDTH = 150;

    static final int BUTTONS_INTERVAL = 4;

    /**
     * Height of buttons on this mod's GUI
     */
    static final int BUTTON_HEIGHT = 20;
    static final int BUTTONS_TRANSLATION_INTERVAL  = BUTTON_HEIGHT + BUTTONS_INTERVAL;

    private final Screen parentScreen;

    private OptionsList optionsList;
    private Button done;
    private Button info;

    public ConfigScreen(final Screen parentScreen) {
        super(Component.translatable("deathstats.configuration.title"));
        this.parentScreen = parentScreen;
    }

    /**
     * Initializes this GUI with options list and buttons.
     */
    @Override
    protected void init() {
        this.optionsList = new OptionsList(
                Objects.requireNonNull(this.minecraft), this.width, this.height,
                OPTIONS_LIST_TOP_HEIGHT,
                this.height - OPTIONS_LIST_BOTTOM_OFFSET,
                OPTIONS_LIST_ITEM_HEIGHT);

        this.optionsList.addBig(OptionInstance.createBoolean("Test", false, value -> {
            System.out.println("Setting " + value);
        }));

        this.addWidget(optionsList);

        info = Button.builder(Component.translatable("deathstats.configuration.moreInfo"), button -> Util.getPlatform().openUri(MORE_INFO_URL))
                .pos(  (this.width - BUTTONS_INTERVAL) / 2 - BOTTOM_BUTTON_WIDTH, this.height - BOTTOM_BUTTON_HEIGHT_OFFSET)
                .size(BOTTOM_BUTTON_WIDTH, BUTTON_HEIGHT).build();

        this.addWidget(info);

        done = Button.builder(Component.translatable("gui.done"), button -> this.onClose())
                .pos((this.width + BUTTONS_INTERVAL) / 2,  this.height - BOTTOM_BUTTON_HEIGHT_OFFSET)
                .size(BOTTOM_BUTTON_WIDTH, BUTTON_HEIGHT).build();

        this.addWidget(done);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        // TODO https://leo3418.github.io/2021/03/31/forge-mod-config-screen-1-16.html
        optionsList.render(guiGraphics, mouseX, mouseY, partialTick);

        info.render(guiGraphics, mouseX, mouseY, partialTick);

        done.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawCenteredString(this.font, this.title.getString(),
                this.width / 2, TITLE_HEIGHT, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }


    @Override
    public void onClose() {
        super.onClose();
    }

}
