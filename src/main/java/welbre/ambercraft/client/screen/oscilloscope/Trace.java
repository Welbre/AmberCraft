package welbre.ambercraft.client.screen.oscilloscope;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.apache.commons.lang3.ArrayUtils;
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

            points[i] = (int) Math.round( Math.clamp(yZero + y, chartPosition.y, yDown));
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
            points[header++] = point;

        //-------------- auto y-scale
        //wait for 10 data and auto-scale the oscilloscope
        if (!isFull && header == 10)
        {
            final var subSet = new double[10];
            System.arraycopy(data,0, subSet, 0, subSet.length);
            double max = Arrays.stream(subSet).max().getAsDouble();
            double min = Arrays.stream(subSet).min().getAsDouble();
            double range = Math.abs(max - min);

            //1.2 is a margin of 20% from the top and bottom of the chart.
            heightScale = range * 1.2 / (2.0 * info.charHeight);
            reComputeAllPoints(info);
        }
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

        for (int x = 0; x < info.chartWidth * widthScale && x < header; x++)
        {
            int fx = Math.toIntExact(Math.round(x / widthScale));
            int y = points[x];

            buffer.addVertex(xLeft + fx, y, 0);
            buffer.setColor(color);
            buffer.setNormal(0, 1, 0);
            buffer.addVertex(xLeft + fx, finalYZero, 0);
            buffer.setColor(color);
            buffer.setNormal(0, 1, 0);
        }
    }
}
