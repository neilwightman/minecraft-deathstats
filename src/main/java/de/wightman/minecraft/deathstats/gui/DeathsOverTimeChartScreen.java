package de.wightman.minecraft.deathstats.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import de.wightman.minecraft.deathstats.DeathStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.h2.mvstore.MVMap;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
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
import java.util.Date;

import static com.mojang.blaze3d.platform.NativeImage.Format.RGBA;

public class DeathsOverTimeChartScreen extends Screen {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeathsOverTimeChartScreen.class);

    private static final int MARGIN = 20;

    private final ResourceLocation chartLocation;
    private final ResourceLocation borderLocation;
    private DynamicTexture texture;
    private java.awt.Font minecraftFont = null;

    // TODO handle parent screens
    public DeathsOverTimeChartScreen() {
        super(Component.translatable("deathstats.name"));
        this.chartLocation = new ResourceLocation("deathstats", "textures/dynamic/chart");
        this.borderLocation = new ResourceLocation("deathstats","textures/gui/borders.png");
        try {
            java.awt.Font customFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, DeathsOverTimeChartScreen.class.getResourceAsStream("/assets/deathstats/font/Minecraft.ttf"));
            this.minecraftFont = customFont.deriveFont(14f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        if (texture == null) {
            updateChartTexture(this.width, this.height);
        }

        Minecraft.getInstance().getTextureManager().bindForSetup(borderLocation);

        guiGraphics.pose().pushPose();
        RenderSystem.defaultBlendFunc();
        guiGraphics.pose().scale(1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, borderLocation);
        RenderSystem.enableBlend();

        // Top and bottom
        for (int x = 8; x < width - 8; x=x+8) {
            guiGraphics.blit(borderLocation, x, 0, 8F, 0.0f, 8, 8, 256, 256);
            guiGraphics.blit(borderLocation,  x, height - 8, 8F, 16.0f, 8, 8, 256, 256);
        }

        // Left and right
        for (int y = 8; y < height - 8; y=y+8) {
            guiGraphics.blit(borderLocation,  0, y, 0F, 8f, 8, 8, 256, 256);
            guiGraphics.blit(borderLocation,  width - 8, y, 16F, 8f, 8, 8, 256, 256);
        }

        // Top left
        guiGraphics.blit(borderLocation,  0, 0, 0.0F, 0.0f, 8, 8, 256, 256);
        // Top right
        guiGraphics.blit(borderLocation,  width - 8, 0, 16.0F, 0.0f, 8, 8, 256, 256);

        // Bottom left
        guiGraphics.blit(borderLocation,  0, height - 8, 0.0F, 16.0f, 8, 8, 256, 256);
        // Bottom right
        guiGraphics.blit(borderLocation,  width - 8, height - 8, 16.0F, 16.0f, 8, 8, 256, 256);


        RenderSystem.disableBlend();
        guiGraphics.pose().popPose();

        // Something went wrong generating the graph
        if (texture != null) {
            guiGraphics.pose().pushPose();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderTexture(0, chartLocation);
            RenderSystem.enableBlend();
            guiGraphics.pose().scale(1.0f, 1.0f, 1.0f);
            // GuiComponent.blit args
            //            int xPos,		    // x position relative to the screen image below it (not the entire screen).
            //            int yPos,		    // y position relative to the screen image below it (not the entire screen).
            //            int blitOffset,	// z position (blitOffSet)
            //            float textureX,	// x position on the texture image to draw from
            //            float textureY,	// y position on the texture image to draw from
            //            int imgSizeX,		// x image size to display (like crop in PS)
            //            int imgSizeY,		// y image size to display (like crop in PS)
            //            int scaleY,		// y image size (will scale image to fit)
            //            int scaleX,		// x image size (will scale image to fit)
            //GuiComponent.blit(poseStack, 0, 0, 0.0F, 0.0f, this.width, this.height, this.width, this.height);
            guiGraphics.blit(chartLocation,  8, 8, 0.0F, 0.0f, this.width - 16, this.height- 16, this.width- 16, this.height- 16);
            RenderSystem.disableBlend();
            guiGraphics.pose().popPose();
        }

        double scale = this.minecraft.getWindow().getGuiScale();

        // drawCenteredString always has drop shadow.

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(width / 2, 15, 0);
        guiGraphics.drawCenteredString(this.font, this.title.getString(), 0, 0, 0xFFFFFF);
        guiGraphics.pose().scale( 0.5f, 0.5f, 0.5f);
        guiGraphics.pose().translate(0, 20, 0);
        guiGraphics.drawString(this.font, "Deaths Over Time", - this.font.width("Deaths Over Time") / 2, 0, 0xFF000000, false);
        guiGraphics.pose().popPose();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate( (18 / scale) + 15, height / 2, 0);
        // undo gui scale factor
        guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(-90.0F));
        guiGraphics.pose().scale( 1.0f / (float)scale, 1.0f / (float)scale, 1.0f / (float)scale);
        // scale text to be fixed based on margin in jfreechart axis space
        guiGraphics.pose().scale( 1.5f, 1.5f, 1.5f);
        //guiGraphics.drawCenteredString(this.font, "Deaths", 0, 0, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Deaths", 0, - this.font.width("Deaths") / 2, 0xFFFFFF, false);
        guiGraphics.pose().popPose();


        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(this.width / 2, height - (18 / scale) - 10 , 0);
        // undo gui scale factor
        guiGraphics.pose().scale( 1.0f / (float)scale, 1.0f / (float)scale, 1.0f / (float)scale);
        // scale text to be fixed based on margin in jfreechart axis space
        guiGraphics.pose().scale( 1.5f, 1.5f, 1.5f);

        guiGraphics.drawString(this.font, "Time", - this.font.width("Time") / 2, 0, 0xFFFFFF, false);
        guiGraphics.pose().popPose();

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        System.out.println("Clicked :" + mouseX + " " + mouseY + " " + button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double p_94688_) {
        System.out.println("Scrolled :" + mouseX + " " + mouseY + " " + p_94688_);
        return super.mouseScrolled(mouseX, mouseX, p_94688_);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double p_94702_, double p_94703_) {
        System.out.println("Dragged :" + mouseX + " " + mouseY + " " + button);
        return super.mouseDragged(mouseX, mouseY, button, p_94702_, p_94703_);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        // update image
        updateChartTexture(width, height);

        super.resize(minecraft, width, height);
    }

    @Override
    public void onClose() {
        super.onClose();

        if (texture != null) {
            this.minecraft.getTextureManager().release(chartLocation);
            texture = null;
        }
    }


    private void updateChartTexture(int width, int height) {
        long start = System.currentTimeMillis();
        JFreeChart chart = createChart(createDataset());
        double scale = this.minecraft.getWindow().getGuiScale();
        BufferedImage image = chart.createBufferedImage((int) (width * scale) - 32, (int) (height * scale) - 32);
        try {
            ByteBuffer buffer = convertImageData(image);
            NativeImage nativeImage = NativeImage.read(RGBA, buffer);

            texture = new DynamicTexture(nativeImage);
            this.minecraft.getTextureManager().register(chartLocation, texture);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            LOGGER.info("Chart time = {}ms", System.currentTimeMillis() - start);
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
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public JFreeChart createChart(TimeSeriesCollection dataset) {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "",            // title
                " ",             // x-axis label
                " ",           // y-axis label
                dataset,            // data
                false,              // create legend?
                false,              // generate tooltips?
                false               // generate URLs?
        );

        //Minecraft gray inv color C6C6C6
        Color lightGray = Color.decode("#C6C6C6");
        // Button gray
        Color gray = Color.decode("#717171");

        chart.setBackgroundPaint(gray);
        chart.getTitle().setFont(minecraftFont);
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(lightGray);
        plot.setDomainGridlinePaint(lightGray);
        plot.setRangeGridlinePaint(lightGray);
        plot.setOutlinePaint(Color.black);
        plot.setAxisOffset(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setDefaultShapesVisible(false);
            renderer.setSeriesPaint(0, Color.BLACK);
            renderer.setSeriesStroke( 0, new BasicStroke(2.0f));
        }

        DateAxis timeAxis = (DateAxis) plot.getDomainAxis();
        timeAxis.setDateFormatOverride(new SimpleDateFormat("d/MM HH:mm"));
        timeAxis.setLabelFont(minecraftFont);
        timeAxis.setTickLabelFont(minecraftFont);
        timeAxis.setTickLabelPaint(Color.black);
        timeAxis.setAxisLinePaint(Color.black);
        timeAxis.setAutoRange(true);
        timeAxis.setAutoTickUnitSelection(true);

        NumberAxis deathAxis = (NumberAxis) plot.getRangeAxis();
        DecimalFormat decimalFormatter = new DecimalFormat("0");
        deathAxis.setNumberFormatOverride(decimalFormatter);
        deathAxis.setAutoRange(true);
        deathAxis.setAutoTickUnitSelection(true);
        deathAxis.setLabelFont(minecraftFont);
        deathAxis.setTickLabelFont(minecraftFont);
        deathAxis.setTickLabelPaint(Color.black);
        deathAxis.setAxisLinePaint(Color.black);

        // TODO green for start session, red for end.
//        TimeSeries s1 = dataset.getSeries(0);
//        double maxY = s1.getMaxY();
//        long x = s1.getTimePeriod(5).getFirstMillisecond();
//        XYLineAnnotation a2 = new XYLineAnnotation(x, 0, x + 1.0, maxY);
//        plot.addAnnotation(a2);
//
//        XYTextAnnotation txt = new XYTextAnnotation( "Started", x, maxY );
//        txt.setTextAnchor(TextAnchor.BASELINE_LEFT);
//        plot.addAnnotation(txt);

        return chart;
    }

    public static TimeSeriesCollection createDataset() {
        final TimeSeries s1 = new TimeSeries("Deaths");

        MVMap<Long, String> log = DeathStats.getInstance().getDeathLog();

        int count = 0;
        for (Long ts : log.keySet()) {
            s1.add(new Millisecond(new Date(ts)), ++count);
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(s1);

        /**
         * TODO Need a second series for times game started and ended.
         * To attach the annotations too
         */


        return dataset;
    }
}
