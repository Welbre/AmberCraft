package welbre.ambercraft.client.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A slider similar to an slide potentiometer.<br>
 * Used to select a value between {@link #high} and {@link #low} dragging a knob.
 * A relative value between 1 and 0 is passed to {@link #interpolation} and used to define the selected value of the slider.
 */
public class Slider extends AbstractWidget
{
    public static final ResourceLocation TEXTURE = ResourceLocation.parse("ambercraft:textures/gui/slider_potentiometer.png");
    private double high, low;
    /// A function that receives a double in an intervale of [1;0] and returns a double in the same intervale.
    /// The received double is where the knob is positioned, zero is the low, and 1 is on the high.
    /// The return of this function then is used to determine the slide value using a linear interpolation.
    public Function<Double, Double> interpolation;
    private BiConsumer<Slider, Double> onValueChange = (slider, value) -> {};
    /// How much of the width can't be used in the knob position.<br> should be a value on {1;0} range!<br>
    /// Exemple, the default texture has 200 pixels in width. However, the first 10 pixels and the last 10 pixels have some details, so,
    /// to avoid the knob be render above my beautiful details, the dead point should be 20 / 200 or 1%. Therefore, 1% of the total length (200)
    /// shouldn't be used to put the knob texture; thus the algorithm will skip the first half deadPoint in the start, and the other half in the end.
    private double deadPoint;
    private int deadPointPixels;
    private int halfDeadPointPixels;
    private double where;

    public Slider(int x, int y, int width, int height, double high, double low, Component message)
    {
        super(x, y, width, height, message);
        this.high = high;
        this.low = low;
        this.interpolation = (v) -> v;//linear
        setDeadPoint(20.0 / 200.0);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        super.onDrag(mouseX, mouseY, dragX, dragY);

        double distanceToOrigin = mouseX - this.getX() - halfDeadPointPixels;

        where = Math.clamp(distanceToOrigin / (this.getWidth() - deadPointPixels), 0, 1);
        onValueChange.accept(this, getValue());
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        guiGraphics.blit(RenderType.GUI_TEXTURED, TEXTURE,
                getX(), getY(),
                0,0,
                width, height,
                200,20,
                200,32);
        //render the knob
        guiGraphics.blit(RenderType.GUI_TEXTURED, TEXTURE,
                (int) (this.getX() + ((this.getWidth()-deadPointPixels) * where) +halfDeadPointPixels - 4),
                this.getY() + (this.getHeight() - 12) / 2,
                0, 20,
                8, 12,
                8, 12,
                200, 32);
    }

    public double getValue()
    {
        if (interpolation == null)
            throw new NullPointerException("The interpolation function is null!");

        Double applied = interpolation.apply(where);
        return (high-low)*applied + low;
    }

    public void setValue(double value)
    {
        setValue(value, true);
    }

    public void setValue(double value, boolean notify)
    {
        double relative = (value-low)/(high-low);
        where = Math.clamp(relative, 0, 1);
        if (notify)
            onValueChange.accept(this, getValue());
    }

    public double getDeadPoint() {
        return deadPoint;
    }

    public void setDeadPoint(double deadPoint) {
        this.deadPoint = Math.clamp(deadPoint, 0, 1);
        this.deadPointPixels = (int) Math.round(this.getWidth() * deadPoint);
        this.halfDeadPointPixels = Math.round(this.deadPointPixels / 2f);
    }

    public void setOnValueChange(BiConsumer<Slider, Double> onValueChange) {
        if (onValueChange == null)
            throw new NullPointerException("The onValueChange function is null!");
        this.onValueChange = onValueChange;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
