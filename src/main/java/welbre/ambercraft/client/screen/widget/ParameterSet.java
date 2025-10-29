package welbre.ambercraft.client.screen.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A helper that has a text, an editbox and a slider to set values.
 */
public final class ParameterSet
{
    public int x, y, width, height;
    public Component message;
    public double value;
    public StringWidget string;
    public EditBox box;
    public Slider slider;
    public Component suggestion;

    public ParameterSet(
            Screen screen,
            int x, int y, int width, int height,
            Component message, Component suggestion, Font font,
            Function<Double, Double> interpolation, double initialValue, double high, double low)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.message = message;
        this.suggestion = suggestion;
        this.value = initialValue;

        box = new EditBox(font, x, y, width, height, message);
        box.setResponder(this::boxResponse);
        box.setFilter(DOUBLE_BOX_FILTER(box));
        box.setValue(String.valueOf(value));

        int textWidth = font.width(message);
        string = new StringWidget(x - textWidth -5, y, textWidth, height, message, font);

        slider = new Slider(x + box.getWidth() + 5, y, width, height, high, low, Component.literal(""));
        slider.setInterpolation(interpolation);
        slider.setOnValueChange(this::sliderOnChange);
        slider.setValue(value, false);

        //forced to add the new widget
        try
        {
            Method addRenderableWidget = Screen.class.getDeclaredMethod("addRenderableWidget", GuiEventListener.class);
            addRenderableWidget.setAccessible(true);
            addRenderableWidget.invoke(screen, string);
            addRenderableWidget.invoke(screen, box);
            addRenderableWidget.invoke(screen, slider);
            addRenderableWidget.setAccessible(false);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void sliderOnChange(Slider slider, double value)
    {
        this.value = value;
        //used doesn't call the editbox responder again and modifies the field
        box.setResponder((s) -> {});
        box.setValue(String.valueOf(value));
        box.moveCursorToStart(false);
        box.setResponder(this::boxResponse);
    }

    public void boxResponse(String string)
    {
        if (string.isEmpty())
        {
            box.setSuggestion(suggestion.getString());
        } else {
            box.setSuggestion("");
            value = Double.parseDouble(string);
            if (slider != null)
                slider.setValue(value, false);
        }
    }

    public static Predicate<String> DOUBLE_BOX_FILTER(EditBox box)
    {
        return (res) -> {
            if (res.isEmpty()) return true;

            try
            {
                Double.parseDouble(res);
                box.setTextColor(0x44cc44);
                return true;
            } catch (NumberFormatException e)
            {
                box.setTextColor(0xcc4444);
                return false;
            }
        };
    }

    public void setVisible(boolean value)
    {
        string.visible = value;
        box.visible = value;
        slider.visible = value;
    }

    public static final class interpolation
    {
        public static double linear(double v)
        {
            return v;
        }
    }
}