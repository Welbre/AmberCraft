package welbre.ambercraft.client.screen.oscilloscope;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import kuse.welbre.tools.Tools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.stream.DoubleStream;

/// A helper to deal with renders the oscilloscope trace.
public class Trace
{
    public boolean isVisible = true;
    public int color = -1;//white
    /// each 1 value means 20 pixels of width
    public double widthScale = 1;
    /// each 1 value means 20 pixels of height
    public double heightScale = 20;
    /// how many pixels the y origin is moved; Positive values move down!
    public double heightOffSet;
    /// how many pixels the x is moved from the zero; Positive means more right.
    public double widthOffSet;
    /// if the oscilloscope displays the values continuously
    public boolean isContinuos = true;

    /// The head of the data, where the next value will be stored.
    public int head = 0;
    public int used = 0;
    public boolean isFresh = true;
    /// stores the raw data value.
    public double[] data;
    /// store a transformed value used directly in the render process.
    public int[] points;

    public Trace(int size, OscilloscopeScreen info)
    {
        this(new double[size], info);
        if (size <= 0)
            throw new IllegalArgumentException("Size must be greater than 0");
    }

    public Trace(double[] data, OscilloscopeScreen info)
    {
        this.data = data;
        this.points = new int[data.length];
    }


    public void clearData()
    {
        isFresh = true;
        head = 0;
        used = 0;
        data = new double[data.length];
        points = new int[points.length];
    }

    /// fit the y-axis in the screen.
    public void autoScaleY(OscilloscopeScreen info)
    {
        double max = Arrays.stream(data).limit(head-1).max().orElse(1);
        double min = Arrays.stream(data).limit(head-1).min().orElse(-1);
        double range = Math.abs(max - min);

        //1.2 is a margin of 20% from the top and bottom of the chart.
        if (Math.abs(range) > 10e-9)
            heightScale = info.charHeight / (range * 1.2);
        else
            heightScale = info.charHeight / (Math.abs(max) * 1.2);

        centralize(info);

        widthOffSet = 0;
        widthScale = (double) data.length / info.chartWidth;
        reComputeAllPoints(info);
    }

    public void centralize(OscilloscopeScreen info)
    {
        double max = Arrays.stream(data).limit(head-1).max().orElse(1);
        double min = Arrays.stream(data).limit(head-1).min().orElse(-1);
        double avg = Arrays.stream(data).limit(head-1).average().orElse(0);
        double range = Math.abs(max - min);

        if (Math.abs(range) > 10e-9)
            heightOffSet = (avg * heightScale);
        else
            heightOffSet = (avg * heightScale / 2.0);
    }

    public void reComputeAllPoints(OscilloscopeScreen info)
    {
        if (data.length == 0)
            return;

        //is on half of the height + the offset scaled
        final double yZeroDelta = heightOffSet + info.charHeight / 2.0;

        var chartPosition = info.chartPosition;
        final int yUp = chartPosition.y;
        final int yDown = chartPosition.y + info.charHeight;

        for (int i = 0; i < points.length; i++)
            points[i] = Math.clamp((int) Math.round(yZeroDelta - (data[i] * heightScale) + info.chartPosition.y), yUp + 2, yDown);
    }

    public void pushData(double value, OscilloscopeScreen info)
    {
        if (data.length == 0)
            return;

        //Compute the y componente of the data relative to the charOrigem info.chartPosition
        //First find the distance to the data zero line

        //is on half of the height + the offset scaled
        final int yUp = info.chartPosition.y;
        final int yDown = yUp + info.charHeight;
        final int y = Math.clamp( getY(info, value), yUp+2, yDown);//the distance to the origin that represents the value in the chart.

        if (isContinuos)
        {
            if (head >= data.length)
            {
                for (int i = 0; i < data.length - 1; i++)
                {
                    data[i] = data[i+1];
                    points[i] = points[i+1];
                }
                head = data.length-1;
            }
        }
        else
        {
            //reset the head to the sample beginning.
            if (head == data.length)
                head = 0;
        }

        data[head] = value;
        points[head] = y;

        head++;

        if (used < data.length)
            used++;

        //-------------- auto y-scale --------------------
        //wait for 10 data and auto-scale the oscilloscope
        if (isFresh && head == 20)
        {
            autoScaleY(info);
            isFresh = false;
        }
    }

    /// Don't change any field in the screen, only read it!
    public void render(MultiBufferSource source, final OscilloscopeScreen info)
    {
        if (!isVisible) return;

        var chartPosition = info.chartPosition;

        final int xLeft = chartPosition.x + 2;
        final int yDown = info.chartPosition.y + info.charHeight;
        final int yZero = (int) ( (info.chartPosition.y + info.charHeight / 2.0) + heightOffSet);

        VertexConsumer buffer;
        //render value lines
        {
            double i = getValue(info, info.chartPosition.y);
            double end = getValue(info, yDown);
            double delta = Math.sqrt(Math.pow(i,2) + Math.pow(end,2)) / 10.0;
            Font font = info.getFont();

            var pose = new PoseStack().last().pose();
            i -= delta;
            if (i < end)
                for (; i < end; i += delta)
                {
                    var txt = Tools.proprietyToSi(i, "V");
                    font.drawInBatch(txt, info.chartPosition.x - 2 - info.getFont().width(txt), getY(info, i) - info.getFont().lineHeight, 0x4499cccc, true, pose, source, Font.DisplayMode.NORMAL, 0, 15728880);
                }
            else
                for (; i > end; i -= delta)
                {
                    var txt = Tools.proprietyToSi(i, "V");
                    font.drawInBatch(txt, info.chartPosition.x - 2 - info.getFont().width(txt), getY(info, i) - info.getFont().lineHeight, 0x4499cccc, true, pose, source, Font.DisplayMode.NORMAL, 0, 15728880);
                }

            //draw cursor text
            if (info.cursor != -1)
            {
                var txt = Tools.proprietyToSi(getValue(info, info.cursor), "V");
                font.drawInBatch(txt, info.chartPosition.x - 2 - info.getFont().width(txt), info.cursor - info.getFont().lineHeight, 0xffdddd00, true, pose, source, Font.DisplayMode.NORMAL, 0, 15728880);
            }

            buffer = source.getBuffer(RenderType.lines());
            //renders the y = 0 line.
            render_line(buffer, info, yZero,0xff00ccff);
            //render cursor line
            if (info.cursor != -1)
                render_line(buffer, info, info.cursor,0xffdddd00);

            i = getValue(info, info.chartPosition.y) - delta;
            if (i < end)
                for (; i < end; i += delta)
                    render_line(buffer, info, getY(info, i), 0x4499cccc);
            else
                for (; i > end; i -= delta)
                    render_line(buffer, info, getY(info, i), 0x4499cccc);
        }

        for (int x = 0; x < (info.chartWidth * widthScale)  && x < used -1; x++)
        {
            int fx = xLeft + Math.round(Math.round(x / widthScale)) + (int) widthOffSet;
            int y = points[x];
            fx = Math.clamp(fx, info.chartPosition.x+2, info.chartPosition.x + info.chartWidth);

            int fx2 = xLeft + Math.round(Math.round((x+1) / widthScale)) + (int) widthOffSet;
            int y2 = points[x + 1];
            fx2 = Math.clamp(fx2, info.chartPosition.x+2, info.chartPosition.x + info.chartWidth);

            buffer.addVertex(fx, y, 0);
            buffer.setColor(color);
            buffer.setNormal(fx2-fx, y2-y, 0);

            buffer.addVertex(fx2, y2, 0);
            buffer.setColor(color);
            buffer.setNormal(fx-fx2, y-y2 ,0);
        }
    }

    private void render_line(VertexConsumer buffer, final OscilloscopeScreen info, final int value, final int color)
    {
        final int yDown = info.chartPosition.y + info.charHeight;

        if (value > info.chartPosition.y && value < yDown)
        {
            buffer.addVertex(new Vector3f(info.chartPosition.x + 2, value, 0));
            buffer.setColor(color);
            buffer.setNormal(1, 0, 0);
            buffer.addVertex(new Vector3f(info.chartPosition.x + info.chartWidth + 2, value, 0));
            buffer.setColor(color);
            buffer.setNormal(1, 0, 0);
        }
    }

    ///Convert a data value to a screen position. Don't clam the value, only return the y relative to the minecraft gui
    private int getY(OscilloscopeScreen info, double value)
    {
        return (int) Math.round(((info.charHeight / 2.0) + heightOffSet) - (value * heightScale) + info.chartPosition.y);
    }

    ///Convert a screen position relative to minecraft gui to a data value.
    private double getValue(OscilloscopeScreen info, int y)
    {
        return (-y + info.chartPosition.y + ((info.charHeight / 2.0) + heightOffSet)) / heightScale;
    }
}
