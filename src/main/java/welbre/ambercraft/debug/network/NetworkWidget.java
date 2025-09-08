package welbre.ambercraft.debug.network;

import com.mojang.math.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.heat.HeatModule;
import welbre.ambercraft.module.network.NetworkModule;
import welbre.ambercraft.sim.heat.HeatNode;

import java.util.ArrayList;
import java.util.List;

public class NetworkWidget extends AbstractWidget {
    public static final int MASTER_COLOR = 0xFF84b067;
    public static final int DEFAULT_COLOR = 0xFFAE8094;
    public static final int MAIN_COLOR = 0XFF7F00FF;
    public static final int ERROR_COLOR = 0xFFDD0A0A;
    public static final int CHILDREN_CONNECTION_COLOR = 0XFF7D86D1;
    public static final int FATHER_CONNECTION_COLOR = 0XFFCB200F;
    public static final int DEFAULT_SIZE = 60;

    public int color = 0;
    public final ModulesHolder holder;
    public final NetworkModule module;
    /// If this module is the block clicked by the network viewer tool.
    public final boolean isMain;
    public boolean shouldRenderToolTip = false;

    //aesthetics
    public Animation animation;
    public Connection[] childConnection = new Connection[0];

    public NetworkWidget(int x, int y,NetworkModule module, ModulesHolder holder, boolean isMain) {
        super(x, y, DEFAULT_SIZE, DEFAULT_SIZE, Component.literal("module"));
        this.module = module;
        this.holder = holder;
        this.isMain = isMain;
        if (worldPosAsString().length() * 5 > this.width)
        {
            var size = (worldPosAsString().length() * 5 + 10);
            this.width = size;
            this.height = size;
        }
        this.color = isMain ? MAIN_COLOR : DEFAULT_COLOR;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        int x = getX();
        int y = getY();

        for (var connection : childConnection)
            connection.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.fill(x-1, y-1, x + width + 1, y + height + 1, 0xff000000);
        guiGraphics.fill(x, y, x+ width, y + height, color);
        if (module.isMaster())
            guiGraphics.fill(x + width - 5, y + width - 5, x + width, y + height, MASTER_COLOR);

        guiGraphics.drawString(Minecraft.getInstance().font, "@" + Integer.toHexString(module.ID), x, y, 0xFFFFFFFF);
        guiGraphics.drawString(Minecraft.getInstance().font, worldPosAsString(), x, y+10, 0xFFFFFFFF);

        if (shouldRenderToolTip)
            RENDER_TOOL_TIPS(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean isMaster = module.getMaster() != null;
        Minecraft.getInstance().debugRenderer.gameTestDebugRenderer
                .addMarker(holder.getBlockPos(), isMaster ? MASTER_COLOR : (isMain ? MAIN_COLOR : DEFAULT_COLOR),
                        "@%x".formatted(module.ID), 2000);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public int[] getCenter() {
        return new int[]{(int) Math.floor((getX() + width / 2.0) + 0.5), (int) Math.floor((getY() + height / 2.0))};
    }

    private String worldPosAsString()
    {
        if (holder == null)
            return "null";
        var worldPos = holder.getBlockPos();
        return "%d,%d,%d".formatted(worldPos.getX(), worldPos.getY(), worldPos.getZ());
    }

    public void warn(Exception exception)
    {
        this.color = ERROR_COLOR;
        AmberCraft.LOGGER.warn(exception.getMessage(), exception);
    }

    public boolean areNodesColliding(NetworkWidget widget) {
        final int x = getX(); final int y = getY();
        final int xw = widget.getX(); final int yw = widget.getY();
        return x < xw + widget.width &&
                x + this.width > xw &&
                y < yw + widget.height &&
                y + this.height > yw;
    }

    public void resolveCollision(NetworkWidget widget, Scheduler scheduler) {
        if (true)//todo remove it
            return;
        final int[] center = getCenter();
        final int[] center0 = widget.getCenter();
        final float radius = 30*1.414f;

        Vec2 c = new Vec2(center[0], center[1]);
        Vec2 c0 = new Vec2(center0[0], center0[1]);
        Vec2 di = c.add(c0.negated());

        if (di.length() < radius*2f)
        {
            Vec2 normal = di.normalized();
            final float delta = radius*2f - di.length();

            Vec2 f = new Vec2(normal.x * delta * 0.5f, normal.y * delta * 0.5f);

            this.setAnimation(scheduler, new Animation(this, (int) (this.getX() + f.x), (int) (this.getY() + f.y),.1f));
            widget.setAnimation(scheduler, new Animation(widget, (int) (widget.getX() - f.x), (int) (widget.getY() - f.y),.1f));
        }
    }

    public void RENDER_TOOL_TIPS(GuiGraphics graphics, int mouseX, int mouseY, float ignoredParcialTick){
        NetworkModule father = module.getFather();
        boolean isMaster = module.getMaster() != null;
        HeatNode heatNode = module instanceof HeatModule heat? heat.getHeatNode() : null;

        ArrayList<Component> list = new ArrayList<>(List.of(
                Component.literal("ID: " + Integer.toHexString(module.ID)).withColor(10494192),
                Component.literal("IsMaster: " + (isMaster ? "true " : "false")).withColor(10494192),
                Component.literal("Father: " + (father == null ? "root" : Integer.toHexString(father.ID))).withColor(10494192),
                Component.literal("Children: ").withColor(10494192)

        ));
        NetworkModule[] children = module.getChildren();
        for (NetworkModule module : children)
            list.add(Component.literal("-->Child: " + Integer.toHexString(module.ID)).withColor(10494192));

        if (holder != null)
            list.add(Component.literal("Pos: x=%d, y=%d, z=%d".formatted(
                    holder.getBlockPos().getX(),
                    holder.getBlockPos().getY(),
                    holder.getBlockPos().getZ()
            )).withColor(DyeColor.LIME.getTextColor()));
        else
            list.add(Component.literal("Pos: null").withColor(DyeColor.LIME.getTextColor()));

        if (heatNode != null)
        {
            list.add(Component.literal("Temperature: %.2f ºC".formatted(heatNode.getTemperature())));
            list.add(Component.literal("Conductivity: %.2f W/ºC".formatted(heatNode.getThermalConductivity())));
            list.add(Component.literal("Capacidade: %.2f J/ºC".formatted(heatNode.getThermalMass())));
            list.add(Component.literal("Ambient temperature: %.2f ºC".formatted(heatNode.getEnvTemperature())));
            list.add(Component.literal("Ambient conductivity: %.2f W/ºC".formatted(heatNode.getEnvConductivity())));
        } else {
            list.add(Component.literal("Heat not is null!").withColor(DyeColor.RED.getTextColor()));
        }

        var last = graphics.pose().last().copy().pose();
        graphics.pose().popPose();
        graphics.renderComponentTooltip(Minecraft.getInstance().font,
                list,
                mouseX, mouseY);
        graphics.pose().pushTransformation(new Transformation(last));
    }

    public void setAnimation(Scheduler scheduler, Animation animation)
    {
        if (this.animation == null)
        {
            this.animation = animation;
            scheduler.add(animation);
        } else
            if (this.animation.isDone())
            {
                scheduler.add(animation);
                this.animation = animation;
            }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
