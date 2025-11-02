package welbre.ambercraft.client.screen.widget;

import com.mojang.blaze3d.platform.InputConstants;
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
import java.util.function.Predicate;

/**
 * A control widget that can set a value using the mouse or the wheel.<br>
 */
public class InfiniteKnob extends AbstractWidget
{
    public static final ResourceLocation TEXTURE = ResourceLocation.parse("ambercraft:textures/gui/knob.png");

    /// The value that the knob in holding
    public double value;
    /// How fast the value changes based on the scrollX and scrolly
    public double[] delta = {1,1};
    /// The circle radius, automatic uses all the space given by the widget
    public double[] radius;
    /// Called each time that the {@link InfiniteKnob#value} changes, pass the knob and the delta.
    public @Nullable BiConsumer<InfiniteKnob, Double> onValueChange = null;
    /**
     * Used to check if a new value that this infinite knob will be set is a valid number.<br>
     * Infinite is too big, use this to restrict the possible values, like positives, between 0 and 1...
     */
    public @Nullable Predicate<Double> valueRestriction = null;

    public InfiniteKnob(int x, int y, int width, int height)
    {
        super(x, y, width, height, Component.literal(""));
        radius = new double[]{width / 2.0, height / 2.0};
    }

    public InfiniteKnob(int x, int y, int width, int height, double[] delta, double value)
    {
        this(x, y, width, height);
        this.delta = delta;
        this.value = value;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        if (isMouseOver(mouseX, mouseY))
        {
            //shift + scroll double the speed
            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344))
            {
                scrollX *= 2;
                scrollY *= 2;
            }


            final double newValue = value + (delta[0] * scrollX) + (delta[1] * scrollY);
            if (valueRestriction != null)
                if (!valueRestriction.test(newValue))
                    return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);

            if (onValueChange != null)
                onValueChange.accept(this, newValue - value);

            value = newValue;
            Minecraft.getInstance().player.playSound(SoundEvents.LEVER_CLICK, 0.4f, 1.8f);
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().rotateAround(new Quaternionf().rotateZ((float) value), getX() + width / 2f, getY() + height / 2f, 0f);
        guiGraphics.blit(
                RenderType.GUI_TEXTURED, TEXTURE,
                getX(), getY(),
                0,0,
                width, height,
                64,64,
                64,128
        );
        guiGraphics.pose().popPose();
    }

    public InfiniteKnob setOnValueChange(BiConsumer<InfiniteKnob, Double>  onValueChange)
    {
        this.onValueChange = onValueChange;
        return this;
    }

    public InfiniteKnob setRestriction(Predicate<Double> restriction)
    {
        this.valueRestriction = restriction;
        return this;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput)
    {

    }
}
