package de.wightman.minecraft.deathstats.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import de.wightman.minecraft.deathstats.DeathStats;
import de.wightman.minecraft.deathstats.record.SessionRecord;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.Range;
import org.jfree.data.time.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        if (texture == null) {
            // Make utility so we can resuse this screen.

            // No Deaths so show this.
            guiGraphics.pose().pushPose();
            guiGraphics.drawCenteredString(this.font, "No deaths",
                    this.width / 2, (this.height /2) - 30, 0xFFFFFF);
            guiGraphics.pose().popPose();

            // TODO add close button too
            return;
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
        guiGraphics.drawString(this.font, Component.translatable("deathstats.overtime.title"), - this.font.width("Deaths Over Time") / 2, 0, 0xFF000000, false);
        guiGraphics.pose().popPose();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate( (18 / scale) + 15, height / 2, 0);
        // undo gui scale factor
        guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(-90.0F));
        guiGraphics.pose().scale( 1.0f / (float)scale, 1.0f / (float)scale, 1.0f / (float)scale);
        // scale text to be fixed based on margin in jfreechart axis space
        guiGraphics.pose().scale( 1.5f, 1.5f, 1.5f);
        //guiGraphics.drawCenteredString(this.font, "Deaths", 0, 0, 0xFFFFFF);
        guiGraphics.drawString(this.font, Component.translatable("deathstats.overtime.axis.deaths"), 0, - this.font.width("Deaths") / 2, 0xFFFFFF, false);
        guiGraphics.pose().popPose();


        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(this.width / 2, height - (18 / scale) - 10 , 0);
        // undo gui scale factor
        guiGraphics.pose().scale( 1.0f / (float)scale, 1.0f / (float)scale, 1.0f / (float)scale);
        // scale text to be fixed based on margin in jfreechart axis space
        guiGraphics.pose().scale( 1.5f, 1.5f, 1.5f);

        guiGraphics.drawString(this.font, Component.translatable("deathstats.overtime.axis.time"), - this.font.width("Time") / 2, 0, 0xFFFFFF, false);
        guiGraphics.pose().popPose();

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double p_94688_) {
        return super.mouseScrolled(mouseX, mouseX, p_94688_);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double p_94702_, double p_94703_) {
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
        if (chart == null) return;

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

    public @Nullable JFreeChart createChart(TimeSeriesCollection dataset) {
        if (dataset == null) return null;

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "",            // title
                " ",             // x-axis label
                " ",           // y-axis label
                dataset,            // data
                false,              // create legend?
                false,              // generate tooltips?
                false               // generate URLs?
        );

        TimeSeries series = dataset.getSeries(0);
        int cnt = series.getItemCount();
        Number maxDeaths = 0.0;

        if (cnt > 0) {
            TimeSeriesDataItem last = series.getDataItem(cnt - 1);

            RegularTimePeriod endDate = last.getPeriod();
            maxDeaths = last.getValue();

            TimeSeriesDataItem first = series.getDataItem(0);
            RegularTimePeriod startDate = first.getPeriod();
            System.out.println(startDate);
            System.out.println(endDate);
        }

        System.out.println(maxDeaths);

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
        timeAxis.setLowerMargin(0.0);

        double deathTickUnit = calculateTickCount( maxDeaths.intValue());
        NumberAxis deathAxis = (NumberAxis) plot.getRangeAxis();
        DecimalFormat decimalFormatter = new DecimalFormat("0");
        deathAxis.setNumberFormatOverride(decimalFormatter);
        deathAxis.setAutoRange(false);
        deathAxis.setAutoTickUnitSelection(false);
        deathAxis.setLabelFont(minecraftFont);
        deathAxis.setTickLabelFont(minecraftFont);
        deathAxis.setTickLabelPaint(Color.black);
        deathAxis.setAxisLinePaint(Color.black);
        deathAxis.setLowerMargin(0.0);

        if (maxDeaths.intValue() > 0) {
            // Ensure we have a tick visible above the last death.
            double ticks = maxDeaths.intValue() / deathTickUnit;
            double upperTickCount = Math.ceil(ticks);
            deathAxis.setRangeWithMargins(new Range(0.0, upperTickCount * deathTickUnit));
        }
        deathAxis.setTickUnit( new NumberTickUnit( deathTickUnit ));

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

    public static double calculateTickCount( int range ) {
        double approximateInterval = (double) range / 10.0;
        int interval = (int) Math.ceil(approximateInterval);
        return (double)Math.max(1, interval);
    }

    public static @Nullable TimeSeriesCollection createDataset() {
        final TimeSeries s1 = new TimeSeries("Deaths");

        int sessionId = DeathStats.getInstance().getActiveSessionId();
        SessionRecord active = DeathStats.getInstance().getSession(sessionId);
        List<Long> res = DeathStats.getInstance().getDeathsPerSession(sessionId);

        if (res.isEmpty()) return null;

        // need to add session start
        s1.add(new Millisecond(new Date(active.start())), 0);

        int count = 0;
        for (Long ts : res) {
            s1.add(new Millisecond(new Date(ts)), ++count);
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(s1);

        return dataset;
    }
}
