package de.wightman.minecraft.deathstats.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
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

    private static final int MARGIN = 20;

    private final ResourceLocation chartLocation;
    private final ResourceLocation borderLocation;
    private DynamicTexture texture;
    private java.awt.Font minecraftFont = null;
    private java.awt.Font minecraftSmallFont = null;

    public ChartScreen() {
        super(Component.translatable("deathstats.name"));
        this.chartLocation = new ResourceLocation("deathstats", "textures/dynamic/chart");
        this.borderLocation = new ResourceLocation("deathstats","textures/gui/borders.png");
        try {
            java.awt.Font customFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, ChartScreen.class.getResourceAsStream("/assets/deathstats/font/Minecraft.ttf"));
            this.minecraftFont = customFont.deriveFont(18f);
            this.minecraftSmallFont = customFont.deriveFont(12f);
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

        guiGraphics.drawCenteredString(this.font, this.title.getString(),
                this.width / 2, 12, 0xFFFFFF);

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
        System.out.println("Resize " + width + "x" + height);
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
        System.out.println("updateChartTexture " + width + "x" + height);
        JFreeChart chart = createChart(createDataset());
        double scale = this.minecraft.getWindow().getGuiScale();
        BufferedImage image = chart.createBufferedImage((int) (width * scale) - 16, (int) (height * scale) - 16);
        try {
            ByteBuffer buffer = convertImageData(image);
            NativeImage nativeImage = NativeImage.read(RGBA, buffer);

            texture = new DynamicTexture(nativeImage);
            this.minecraft.getTextureManager().register(chartLocation, texture);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static ByteBuffer convertImageData(BufferedImage bi) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            // TODO can we write straight to the bytebuffer?
            boolean write = ImageIO.write(bi, "PNG", out);
            System.out.println("write = " + write);
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

    public JFreeChart createChart(XYDataset dataset) {
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

        DateAxis timeAxis = (DateAxis) plot.getDomainAxis();
        timeAxis.setDateFormatOverride(new SimpleDateFormat("HH"));
        timeAxis.setTickUnit(new DateTickUnit(DateTickUnitType.HOUR, 1));
        System.out.println("Height in update " + this.height);
        timeAxis.setLabelFont( this.height < 640 ? minecraftSmallFont : minecraftFont);
        timeAxis.setTickLabelFont(minecraftSmallFont);
        timeAxis.setLabelPaint(Color.black);

        NumberAxis deathAxis = (NumberAxis) plot.getRangeAxis();
        DecimalFormat decimalFormatter = new DecimalFormat("0");
        deathAxis.setNumberFormatOverride(decimalFormatter);
        deathAxis.setTickUnit(new NumberTickUnit(1.0));
        System.out.println("Width in update " + this.width);
        deathAxis.setLabelFont( this.width < 640 ? minecraftSmallFont : minecraftFont);
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
