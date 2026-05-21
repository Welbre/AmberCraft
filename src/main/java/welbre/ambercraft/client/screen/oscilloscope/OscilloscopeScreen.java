package welbre.ambercraft.client.screen.oscilloscope;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import welbre.ambercraft.client.screen.widget.InfiniteKnob;
import welbre.ambercraft.network.OscilloscopeDataPayload;
import welbre.ambercraft.network.oscilloscope.OscilloscopeClosedPayload;

import java.util.Arrays;
import java.util.List;

public class OscilloscopeScreen extends Screen
{
    public boolean isAutomaticYScale = true;

    public int chartWidth = 480;
    public int charHeight = chartWidth * 9 / 16;//270 pixels
    /// where the chart origin is positioned
    public Vector2i chartPosition;

    public int cursor = -1;
    public boolean isPaused = false;
    public final int oscilloscope_id;


    public Trace[] traces = new Trace[0];

    public OscilloscopeScreen(FriendlyByteBuf buf)
    {
        super(Component.literal("Oscilloscope"));
        oscilloscope_id = buf.readInt();
    }

    @Override
    protected void init() {
        super.init();
        chartPosition = new Vector2i((width - chartWidth) / 2,(height - charHeight) / 2);
        addRenderableWidget(new InfiniteKnob(100 ,100 ,50 ,50, new double[]{0.01,0.01}, 1).setOnValueChange(this::zoomXAxes));
        addRenderableWidget(new InfiniteKnob(100 ,150 ,50 ,50, new double[]{0.01,0.01}, 20).setOnValueChange(this::moveXAxes));
        addRenderableWidget(new InfiniteKnob(100 ,200 ,50 ,50, new double[]{0.01,0.01}, 0).setOnValueChange(this::zoomYAxes));
        addRenderableWidget(new InfiniteKnob(100 ,250 ,50 ,50, new double[]{0.01,0.01}, 0).setOnValueChange(this::moveYAxes));
        Button.Builder clear = Button.builder(Component.literal("clear"), button -> {
            clearData();
        });
        Button.Builder mode = Button.builder(Component.literal("mode"), button -> {
            for (var trace : traces)
                trace.isContinuos = !trace.isContinuos;
        });
        Button.Builder pause = Button.builder(Component.literal("pause"), button -> {
            isPaused = !isPaused;
        });
        Button.Builder disconnectProbe = Button.builder(Component.literal("disconnect"), button -> {
            OscilloscopeDataPayload.DATA.remove(oscilloscope_id);
            PacketDistributor.sendToServer(new OscilloscopeClosedPayload());
        });

        addRenderableWidget(clear.bounds(100, 300, 50, 50).build());
        addRenderableWidget(mode.bounds(100, 360, 50, 50).build());
        addRenderableWidget(pause.bounds(100, 410, 50, 50).build());
        addRenderableWidget(disconnectProbe.bounds(100, 470, 100, 50).build());

        //update all trace data.
        List<OscilloscopeDataPayload.DataTrace> data = OscilloscopeDataPayload.DATA.get(oscilloscope_id);
        if (data != null)
        {
            traces = new Trace[data.size()];

            for (OscilloscopeDataPayload.DataTrace dTrace : data)
                traces[dTrace.id] = new Trace(
                        dTrace.head >= 1000 ? Arrays.copyOfRange(dTrace.data, dTrace.head-1000, dTrace.head) : Arrays.copyOf(dTrace.data, 1000),
                        dTrace.head >= 1000 ? 1000 : dTrace.head,
                        this
                );
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        //check if they invalidated the data. if's true, can close all probe processes.
        if (!OscilloscopeDataPayload.DATA.containsKey(oscilloscope_id))
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
            //render traces
            for (Trace trace : traces)
                trace.render(source, this);

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

    /// Push fresh data from the payloads.
    public void updateData(OscilloscopeDataPayload.DataTrace trace)
    {
        if (isPaused)
            return;

        //check if the total off screen traces don't math with the data traces.
        if (traces.length < OscilloscopeDataPayload.DATA.get(oscilloscope_id).size())
        {
            var len = OscilloscopeDataPayload.DATA.get(oscilloscope_id).size();
            var temp = new Trace[len];
            System.arraycopy(traces, 0, temp, 0, traces.length);
            temp[len-1] = new Trace(1000, this);

            traces = temp;
        }

        for (;trace.bottom < trace.head; trace.bottom++)
        {
            traces[trace.id].pushData(trace.data[trace.bottom], this);

            computeMaxAndMin(trace.data[trace.bottom]);
        }
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
                trace.centralize(this);
            return true;
        }
        if (keyCode == InputConstants.KEY_C)
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (mouseX >= chartPosition.x && mouseX <= chartPosition.x + chartWidth && mouseY >= chartPosition.y && mouseY <= chartPosition.y + charHeight)
            cursor = (int) Math.round(mouseY);
        else
            cursor = -1;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void zoomXAxes(InfiniteKnob infiniteKnob, double delta)
    {
        for (Trace trace : traces)
        {
            trace.widthScale += delta;
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
