package welbre.ambercraft.client.screen.oscilloscope;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3f;
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        //knob control
        //todo refactor this to using a dedicated widget
        if (isInChart(mouseX, mouseY))
        {

            if (hasControlDown())//zoom
                if (hasShiftDown())//zoom in the x axes
                {
                    for (Trace trace : traces)
                        trace.widthScale *= scrollY < 0 ? 2 : 0.5;
                }
                else
                {
                    for (Trace trace : traces)//zoom in the y axes
                        trace.heightScale *= scrollY < 0 ? 2 : 0.5;
                }
            else if (hasShiftDown())//move x
                for (Trace trace : traces)
                    trace.widthOffSet -= 5 * (int) scrollY;
            else//move y
                for (Trace trace : traces)
                    trace.heightOffSet -= 5 * (int) scrollY;

            for (Trace trace : traces)
                trace.reComputeAllPoints(this);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
