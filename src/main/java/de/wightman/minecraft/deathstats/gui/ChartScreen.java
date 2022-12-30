package de.wightman.minecraft.deathstats.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.time.Hour;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import static com.mojang.blaze3d.platform.NativeImage.Format.RGBA;

public class ChartScreen extends Screen {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChartScreen.class);

    private final ResourceLocation chartLocation;
    private final ResourceLocation borderLocation;
    private Button done;
    private DynamicTexture texture;
    private java.awt.Font minecraftFont = null;
    private java.awt.Font minecraftSmallFont = null;
    private int lastChartWidth = -1;
    private int lastChartHeight = -1;

    private static final int MARGIN = 20;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTONS_INTERVAL = 4;
    private static final int BOTTOM_BUTTON_WIDTH = 150;
    private static final int BOTTOM_BUTTON_HEIGHT_OFFSET = 26;

    public ChartScreen() {
        super(new TranslatableComponent("deathstats.name"));
        this.chartLocation = new ResourceLocation("deathstats", "textures/dynamic/chart");
        this.borderLocation = new ResourceLocation("deathstats","textures/gui/borders.png");
        try {
            java.awt.Font customFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, ChartScreen.class.getResourceAsStream("/assets/deathstats/font/Minecraft.ttf"));
            this.minecraftFont = customFont.deriveFont(18f);
            this.minecraftSmallFont = customFont.deriveFont(12f);
        } catch (Exception e) {
            LOGGER.error("Cannot load fonts from assets.", e);
        }
    }

    @Override
    protected void init() {
        super.init();

        done = new Button(
                (this.width + BUTTONS_INTERVAL) / 2,
                this.height - BOTTOM_BUTTON_HEIGHT_OFFSET,
                BOTTOM_BUTTON_WIDTH, BUTTON_HEIGHT,
                new TranslatableComponent("gui.done"),
                button -> this.onClose());

        this.addWidget(done);

        updateChartTexture(this.width, this.height);
    }


    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);

        Minecraft.getInstance().getTextureManager().bindForSetup(borderLocation);

        RenderSystem.setShaderTexture(0, borderLocation);

        int chartTop = MARGIN;
        int chartBottom = this.height - BOTTOM_BUTTON_HEIGHT_OFFSET - BUTTONS_INTERVAL;
        int chartLeft = MARGIN;
        int chartRight = this.width - MARGIN;

        // Top and bottom
        for (int x = chartLeft + 8; x < chartRight - 8; x=x+8) {
            GuiComponent.blit(poseStack, x, chartTop, 8F, 0.0f, 8, 8, 256, 256);
            GuiComponent.blit(poseStack, x, chartBottom - 8, 8F, 16.0f, 8, 8, 256, 256);
        }

        // Left and right
        for (int y = chartTop + 8; y < chartBottom - 8; y=y+8) {
            GuiComponent.blit(poseStack, chartLeft, y, 0F, 8f, 8, 8, 256, 256);
            GuiComponent.blit(poseStack, chartRight - 8, y, 16F, 8f, 8, 8, 256, 256);
        }

        // Top left
        GuiComponent.blit(poseStack, chartLeft, chartTop, 0.0F, 0.0f, 8, 8, 256, 256);
        // Top right
        GuiComponent.blit(poseStack, chartRight - 8, chartTop, 16.0F, 0.0f, 8, 8, 256, 256);

        // Bottom left
        GuiComponent.blit(poseStack, chartLeft, chartBottom - 8, 0.0F, 16.0f, 8, 8, 256, 256);
        // Bottom right
        GuiComponent.blit(poseStack, chartRight - 8, chartBottom - 8, 16.0F, 16.0f, 8, 8, 256, 256);

        // Something went wrong generating the graph
        if (texture != null) {
            RenderSystem.setShaderTexture(0, chartLocation);

            int chartHeight = chartBottom - chartTop - 16;
            int chartWidth = chartRight - chartLeft - 16;

            GuiComponent.blit(poseStack, chartLeft + 8, chartTop + 8, 0.0F, 0.0f,
                    chartWidth, chartHeight, chartWidth, chartHeight);
        }

        drawCenteredString(poseStack, this.font, this.title.getString(),
                this.width / 2, 8, 0xFFFFFF);

        done.render(poseStack, mouseX, mouseY, partialTick);

        super.render(poseStack, mouseX, mouseY, partialTick);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        LOGGER.debug("mouseClicked {}x{}", mouseX, mouseY);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double p_94688_) {
        LOGGER.debug("mouseScrolled {}x{}", mouseX, mouseY);
        return super.mouseScrolled(mouseX, mouseX, p_94688_);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double p_94702_, double p_94703_) {
        LOGGER.debug("mouseDragged {}x{}", mouseX, mouseY);
        return super.mouseDragged(mouseX, mouseY, button, p_94702_, p_94703_);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        LOGGER.debug("mouseReleased {}x{}", mouseX, mouseY);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        super.onClose();

        if (texture != null) {
            this.minecraft.getTextureManager().release(chartLocation);
            texture = null;
        }
    }


    private void updateChartTexture(int chartWidth, int chartHeight) {
        lastChartWidth = chartWidth;
        lastChartHeight = chartHeight;

        final long start = System.nanoTime();
        LOGGER.debug("updateChartTexture {}x{}", chartWidth, chartHeight);
        double scale = this.minecraft.getWindow().getGuiScale();
        JFreeChart chart = createChart(createDataset(), chartWidth * (int)scale, chartHeight * (int)scale);

        int chartTop = MARGIN;
        int chartBottom = chartHeight - BOTTOM_BUTTON_HEIGHT_OFFSET - BUTTONS_INTERVAL;
        int chartLeft = MARGIN;
        int chartRight = chartWidth - MARGIN;

        chartWidth = chartRight - chartLeft - 16;
        chartHeight = chartBottom - chartTop - 16;

        BufferedImage image = chart.createBufferedImage((int) (chartWidth * scale) - 16, (int) (chartHeight * scale) - 16);
        try {
            ByteBuffer buffer = convertImageData(image);
            NativeImage nativeImage = NativeImage.read(RGBA, buffer);

            texture = new DynamicTexture(nativeImage);
            // Register frees the old texture
            this.minecraft.getTextureManager().register(chartLocation, texture);
        } catch (IOException ioe) {
            LOGGER.error("Failed to generate chart.", ioe);
        } finally {
            double duration = ((double)System.nanoTime() - (double)start) / 1_000_000_000.0f;
            LOGGER.debug("updateChartTexture duration = {}s", duration );
        }
    }

    public static ByteBuffer convertImageData(BufferedImage bi) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            // TODO can we write straight to the bytebuffer?
            boolean write = ImageIO.write(bi, "PNG", out);
            byte[] data = out.toByteArray();
            ByteBuffer direct = ByteBuffer.allocateDirect(data.length);
            direct.put(data, 0, data.length);
            direct.rewind();
            return direct;
        } catch (IOException ioe) {
            LOGGER.error("Failed to convert chart.", ioe);
        }
        return null;
    }

    public JFreeChart createChart(XYDataset dataset, int chartWidth, int chartHeight) {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "",            // title
                "Time",             // x-axis label
                "Deaths",           // y-axis label
                dataset,            // data
                false,              // create legend?
                false,              // generate tooltips?
                false               // generate URLs?
        );

        //Minecraft gray inv color C6C6C6
        Color gray = Color.decode("#C6C6C6");

        chart.setBackgroundPaint(gray);
        chart.getTitle().setFont(minecraftFont);
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(gray);
        plot.setDomainGridlinePaint(gray);
        plot.setRangeGridlinePaint(gray);
        plot.setOutlinePaint(gray);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setDefaultShapesVisible(false);
            renderer.setSeriesPaint(0, Color.BLACK);
        }

        boolean isSmall = chartWidth <= 600 || chartHeight <= 400;
        LOGGER.debug("IsSmall = {} {}x{}", isSmall, chartWidth, chartHeight);

        DateAxis timeAxis = (DateAxis) plot.getDomainAxis();
        timeAxis.setDateFormatOverride(new SimpleDateFormat("HH"));
        timeAxis.setTickUnit(new DateTickUnit(DateTickUnitType.HOUR, 1));
        timeAxis.setLabelFont( isSmall ? minecraftSmallFont : minecraftFont );
        timeAxis.setTickLabelFont(minecraftSmallFont);
        timeAxis.setLabelPaint(Color.black);

        NumberAxis deathAxis = (NumberAxis) plot.getRangeAxis();
        DecimalFormat decimalFormatter = new DecimalFormat("0");
        deathAxis.setNumberFormatOverride(decimalFormatter);
        deathAxis.setTickUnit(new NumberTickUnit(1.0));
        deathAxis.setLabelFont( isSmall ? minecraftSmallFont : minecraftFont );
        deathAxis.setTickLabelFont(minecraftSmallFont);
        deathAxis.setLabelPaint(Color.black);

        return chart;
    }

    public static XYDataset createDataset() {
        final TimeSeries s1 = new TimeSeries("Deaths");
        s1.add(new Hour( 10, 1,1,1978), 5);
        s1.add(new Hour( 12, 1,1,1978), 10);
        s1.add(new Hour( 15, 1,1,1978), 1);

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(s1);

        return dataset;
    }
}
