package welbre.ambercraft.client.screen.oscilloscope;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Vector3f;

import java.util.Arrays;

/// A helper to deal with render the oscilloscope trace.
public class Trace
{
    public boolean isVisible = true;
    public int color = -1;//white
    /// each 1 value means 20 pixels of width
    public double widthScale = 1;
    /// each 1 value means 20 pixels of height
    public double heightScale = 20;
    /// how many pixels the y is moved; Positive values move down!
    public double heightOffSet;
    /// how many pixels the x is moved from the zero; Positive means more right.
    public double widthOffSet;

    /// The header of the data, where the next value will be stored.
    public int header = 0;
    public boolean isFull = false;
    public double[] data;

    public int[] points;

    public Trace(int size, OscilloscopeScreen info)
    {
        this(new double[size], info);
        if (size == 0)
            throw new IllegalArgumentException("Size must be greater than 0");
    }

    public Trace(double[] data, OscilloscopeScreen info)
    {
        this.data = data;
        this.points = new int[info.chartWidth];
        this.heightOffSet = info.charHeight / 2.0;
    }


    public void clearData()
    {
        isFull = false;
        header = 0;
        data = new double[data.length];
        points = new int[points.length];
    }

    /// fit the y-axis in the screen.
    public void autoScaleY(OscilloscopeScreen info)
    {
        double max = Arrays.stream(data).max().getAsDouble();
        double min = Arrays.stream(data).min().getAsDouble();
        double range = Math.abs(max - min);
        if (range == 0)
            return;

        //1.2 is a margin of 20% from the top and bottom of the chart.
        heightScale = range * 1.2 / (2.0 * info.charHeight);
        reComputeAllPoints(info);
    }
    public void reComputeAllPoints(OscilloscopeScreen info)
    {
        if (data.length == 0)
            return;

        var chartPosition = info.chartPosition;
        final int yZero = chartPosition.y + (int) Math.round(heightOffSet);
        final int yDown = chartPosition.y + info.charHeight;
        for (int i = 0; i < points.length; i++)
        {
            final double y = data[i] / 2.0 / heightScale;
            final int height = (int) Math.round(Math.clamp(yZero + y, chartPosition.y, yDown));

            if (i + widthOffSet >= points.length)
                points[(int) Math.round(i + widthOffSet - points.length)] = height;
            else if (i + widthOffSet < 0)
                points[(int) Math.round(i + points.length + widthOffSet)] = height;
            else
                points[(int) Math.round(i + widthOffSet)] = height;
        }
    }

    public void pushData(double value, OscilloscopeScreen info)
    {
        if (data.length == 0)
            return;
        //find the data end.
        if (data.length == header)
        {
            isFull = true;
            header = 0;
        }
        data[header] = value;

        var chartPosition = info.chartPosition;
        final int yZero = chartPosition.y + (int) Math.round(heightOffSet);
        final int yDown = chartPosition.y + info.charHeight;

        final double y = value / 2.0 / heightScale;
        final int point = (int) Math.round( Math.clamp(yZero + y, chartPosition.y, yDown) );

        if (header >= points.length)
        {
            final int[] nPoints = new int[points.length];
            //todo fix used a better approach, maybe a "end less" array
            if (nPoints.length - 1 >= 0)
                System.arraycopy(points, 1, nPoints, 0, nPoints.length-1);
            points = nPoints;
            points[points.length-1] = point;
        }
        else
            if (header + widthOffSet >= points.length)
                points[(int) Math.round(header++ + 1 - widthOffSet)] = point;
            else
                points[(int) Math.round(header++ + widthOffSet)] = point;

        //-------------- auto y-scale
        //wait for 10 data and auto-scale the oscilloscope
        if (!isFull && header == 10)
            autoScaleY(info);
    }

    /// Don't change any field in the screen, only read it!
    public void render(VertexConsumer buffer, final OscilloscopeScreen info)
    {
        if (!isVisible) return;

        var chartPosition = info.chartPosition;

        final int xLeft = chartPosition.x + 2;
        final int yZero = chartPosition.y + (int) Math.round(heightOffSet);
        final int yDown = chartPosition.y + info.charHeight;

        final int finalYZero = Math.clamp(yZero, chartPosition.y, yDown);

        //renders the y = 0 line.
        //if (chartPosition.y + heightOffSet > chartPosition.y && chartPosition.y + heightOffSet < chartPosition.y + info.charHeight)
        if (heightOffSet > 0 && heightOffSet < info.charHeight)
        {
            buffer.addVertex(new Vector3f(chartPosition.x + 2, chartPosition.y + (int) heightOffSet, 0));
            buffer.setColor(0xff00ccff);
            buffer.setNormal(1, 0, 0);
            buffer.addVertex(new Vector3f(chartPosition.x + info.chartWidth + 2, chartPosition.y + (int) heightOffSet, 0));
            buffer.setColor(0xff00ccff);
            buffer.setNormal(1, 0, 0);
        }

        for (int x = 0; x < (info.chartWidth * widthScale)  && x < header -1; x++)
        {
            int fx = xLeft + Math.round(Math.round(x / widthScale));
            int y = points[x];

            int fx2 = xLeft + Math.round(Math.round((x+1) / widthScale));
            int y2 = points[x + 1];

            buffer.addVertex(fx, y, 0);
            buffer.setColor(color);
            buffer.setNormal(fx2-fx, y2-y, 0);

            buffer.addVertex(fx2, y2, 0);
            buffer.setColor(color);
            buffer.setNormal(fx-fx2, y-y2 ,0);
        }
    }
}
