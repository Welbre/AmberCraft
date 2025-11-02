package welbre.ambercraft.client.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FiniteKnob extends AbstractWidget
{
    public static final ResourceLocation TEXTURE = ResourceLocation.parse("ambercraft:textures/gui/knob.png");

    /// The value that the knob in holding
    public double value;
    /// The max value that this node can be set.
    public double max;
    /// The min value that this node can be set
    public double min;
    /// How fast the value changes based on the scrollX and scrolly
    public double[] delta = {1,1};
    /// The circle radius, automatic uses all the space given by the widget
    public double[] radius;
    /// Called each time that the {@link FiniteKnob#value} changes, pass the knob and the delta.
    public @Nullable BiConsumer<FiniteKnob, Double> onValueChange = null;

    public FiniteKnob(int x, int y, int width, int height, double max, double min)
    {
        super(x, y, width, height, Component.literal(""));
        if (max <= min)
            throw new IllegalArgumentException("The max value must be greater than the min value!");
        radius = new double[]{width / 2.0, height / 2.0};
        this.max = max;
        this.min = min;
        this.value = (max + min) / 2;
    }

    public FiniteKnob(int x, int y, int width, int height, double max, double min, double[] delta)
    {
        this(x, y, width, height, max, min);
        this.delta = delta;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        if (isMouseOver(mouseX, mouseY))
        {
            double newValue = Math.clamp(value + (delta[0] * scrollX) + (delta[1] * scrollY), min, max);
            if (newValue != value)
                Minecraft.getInstance().player.playSound(SoundEvents.LEVER_CLICK, 0.4f, 1.8f);
            value = newValue;
            if (onValueChange != null)
                onValueChange.accept(this, newValue - value);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().rotateAround(new Quaternionf().rotateZ(
                (float) ( ((value-min)/(max-min)) * Math.PI * 2.0 + Math.PI))
                , getX() + width / 2f, getY() + height / 2f, 0f);
        guiGraphics.blit(
                RenderType.GUI_TEXTURED, TEXTURE,
                getX(), getY(),
                0,0,
                width, height,
                64,64,
                64,128
        );
        guiGraphics.pose().popPose();
        guiGraphics.blit(
                RenderType.GUI_TEXTURED, TEXTURE,
                getX(), getY(),
                0,64,
                width, height,
                64,64,
                64,128
        );
    }

    public FiniteKnob setOnValueChange(BiConsumer<FiniteKnob, Double> onValueChange)
    {
        this.onValueChange = onValueChange;
        return this;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput)
    {

    }
}
