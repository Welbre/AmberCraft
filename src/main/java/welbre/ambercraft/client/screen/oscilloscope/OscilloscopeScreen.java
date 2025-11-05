package welbre.ambercraft.client.screen.oscilloscope;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import welbre.ambercraft.client.screen.widget.InfiniteKnob;
import welbre.ambercraft.network.oscilloscope.OscilloscopeClosedPayload;

public class OscilloscopeScreen extends Screen
{
    public boolean isAutomaticYScale = true;

    public int chartWidth = 480;
    public int charHeight = chartWidth * 9 / 16;//270 pixels
    public Vector2i chartPosition;



    public Trace[] traces = new Trace[]{new Trace(1000, this)};

    public OscilloscopeScreen(FriendlyByteBuf buf)
    {
        super(Component.literal("Oscilloscope"));
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new StringWidget(Component.literal("olá"), font));
        chartPosition = new Vector2i((width - chartWidth) / 2,(height - charHeight) / 2);
        addRenderableWidget(new InfiniteKnob(100 ,100 ,50 ,50, new double[]{0.01,0.01}, 1).setOnValueChange(this::zoomXAxes).setRestriction(d -> {return d > 0;}));
        addRenderableWidget(new InfiniteKnob(100 ,150 ,50 ,50, new double[]{0.01,0.01}, 20).setOnValueChange(this::moveXAxes).setRestriction(d -> {return d > 0;}));
        addRenderableWidget(new InfiniteKnob(100 ,200 ,50 ,50, new double[]{0.01,0.01}, 0).setOnValueChange(this::zoomYAxes).setRestriction(d -> {return true;}));
        addRenderableWidget(new InfiniteKnob(100 ,250 ,50 ,50, new double[]{0.01,0.01}, 0).setOnValueChange(this::moveYAxes).setRestriction(d -> {return true;}));
        Button.Builder builder = Button.builder(Component.literal("clear"), button -> {
            for (Trace trace : traces)
            {
                trace.isContinuos = !trace.isContinuos;
            }
        });
        addRenderableWidget(builder.bounds(100, 300, 50, 50).build());
    }

    @Override
    public void onClose() {
        super.onClose();
        PacketDistributor.sendToServer(new OscilloscopeClosedPayload());
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.fill(chartPosition.x, chartPosition.y, chartPosition.x + chartWidth , chartPosition.y + charHeight, 0x88000000);
        graphics.renderOutline(chartPosition.x, chartPosition.y, chartWidth ,charHeight, 0xff00ff00);
        graphics.drawSpecial(this::drawChart);
        graphics.flush();
    }

    public void drawChart(MultiBufferSource source)
    {
        try
        {
            VertexConsumer buffer = source.getBuffer(RenderType.lines());

            //render traces
            for (Trace trace : traces)
                trace.render(buffer, this);

        } catch (Exception a)
        {
            a.printStackTrace();
        }

    }

    public void clearData()
    {
        for (Trace trace : traces)
            trace.clearData();
    }

    public void updateData(double value)
    {
        traces[0].pushData(value, this);

        computeMaxAndMin(value);
    }

    public void computeMaxAndMin(double value)
    {
        if (!isAutomaticYScale)
            return;
    }

    public boolean isInChart(double mouseX, double mouseY)
    {
        return mouseX >= chartPosition.x && mouseX <= chartPosition.x + chartWidth && mouseY >= chartPosition.y && mouseY <= chartPosition.y + charHeight;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        //auto y-scale
        if (keyCode == InputConstants.KEY_SPACE)
        {
            for (Trace trace : traces)
                trace.autoScaleY(this);
            return true;
        }
        //resetData
        if (keyCode == InputConstants.KEY_R && hasControlDown())
        {
            for (Trace trace : traces)
                trace.clearData();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public void zoomXAxes(InfiniteKnob infiniteKnob, double delta)
    {
        for (Trace trace : traces)
        {
            trace.widthScale = infiniteKnob.value;
            trace.reComputeAllPoints(this);
        }
    }

    public void moveXAxes(InfiniteKnob infiniteKnob, double delta)
    {
        for (Trace trace : traces)
        {
            trace.widthOffSet -= 50 * delta;
            trace.reComputeAllPoints(this);
        }
    }

    public void zoomYAxes(InfiniteKnob infiniteKnob, double delta)
    {
        for (Trace trace : traces)
        {
            trace.heightScale *= delta < 0 ? 2 : 0.5;
            trace.reComputeAllPoints(this);
        }
    }

    public void moveYAxes(InfiniteKnob infiniteKnob, double delta)
    {
        for (Trace trace : traces)
        {
            trace.heightOffSet -= 50 * delta;
            trace.reComputeAllPoints(this);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
