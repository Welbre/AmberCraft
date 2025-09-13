package welbre.ambercraft.debug.network;

import com.mojang.math.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.DebugToolInfo;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.network.NetworkModule;

import java.util.ArrayList;

public class NetworkWidget extends AbstractWidget {
    public static final int MASTER_COLOR = 0xFF84b067;
    public static final int DEFAULT_COLOR = 0xFFAE8094;
    public static final int[] DEFAULT_EXTRA_COLORS = {0XFF7D0D7D,0XFFF8D57E,0XFF336B29,0XFFFFBFD4,0XFFE1C03C,0XFFE0ACA2,0XFF9EA35A};
    public static final int MAIN_COLOR = 0XFF7F00FF;
    public static final int ERROR_COLOR = 0xFFDD0A0A;
    public static final int WARN_COLOR = 0xFFFFCC00;
    public static final int CHILDREN_CONNECTION_COLOR = 0XFF7D86D1;
    public static final int FATHER_CONNECTION_COLOR = 0XFFCB200F;
    public static final int DEFAULT_SIZE = 60;

    public int color = 0;
    public final ModulesHolder holder;
    /// The module used by the server, transferred by the package
    public final NetworkModule serverModule;
    /// The client version of the module
    public final NetworkModule clientModule;
    /// Used to show information.
    public NetworkModule active;
    public NetworkWidget root;
    /// If this module is the block clicked by the network viewer tool.
    public final boolean isMain;
    public boolean shouldRenderToolTip = false;
    /// A list of warning showed in the tooltip.
    public final ArrayList<String> warn = new ArrayList<>();
    /// A list of errors showed in the tooltip.
    public final ArrayList<String> errors = new ArrayList<>();

    //aesthetics
    public Animation animation;
    public Connection[] childConnection = new Connection[0];

    public NetworkWidget(int x, int y, NetworkModule serverModule, NetworkModule clientModule, ModulesHolder holder, boolean isMain) {
        super(x, y, DEFAULT_SIZE, DEFAULT_SIZE, Component.literal("module"));
        this.serverModule = serverModule;
        this.clientModule = clientModule;
        this.active = serverModule;
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
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        int x = getX();
        int y = getY();

        for (var connection : childConnection)
            connection.render(graphics, mouseX, mouseY, partialTick);

        graphics.fill(x-1, y-1, x + width + 1, y + height + 1, 0xff000000);
        graphics.fill(x, y, x+ width, y + height, color);
        if (serverModule.isMaster())
            graphics.fill(x + width - 5, y + width - 5, x + width, y + height, MASTER_COLOR);

        graphics.drawString(Minecraft.getInstance().font, "@" + Integer.toHexString(serverModule.ID), x, y, 0xFFFFFFFF);
        graphics.drawString(Minecraft.getInstance().font, worldPosAsString(), x, y+10, 0xFFFFFFFF);

        if (shouldRenderToolTip)
        {
            RENDER_TOOL_TIPS(graphics, mouseX, mouseY, partialTick);
            RENDER_FATHER_ARROW(graphics, mouseX, mouseY, partialTick);
        }

        //fixme, at the moment the render is incorrect, they are rendering faces that is behind other faces, and other culling problems
        graphics.pose().pushPose();
        var p = graphics.pose();
        p.translate(x + width/2f - 10,y + height/2f,10);
        p.rotateAround(new Quaternionf().rotateY((float) Math.toRadians(45)).rotateLocalX((float) Math.toRadians(30)),10f,10f,10f);
        p.scale(20,20, 20);

        graphics.drawSpecial(drawer -> {
            BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(holder.getBlockState());
            ModelData data = model.getModelData(holder.getLevel(), holder.getBlockPos(), holder.getBlockState(), holder.getModelData());
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(holder.getBlockState(), p, drawer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, data, null);
        });

        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean isMaster = serverModule.getMaster() != null;
        Minecraft.getInstance().debugRenderer.gameTestDebugRenderer
                .addMarker(holder.getBlockPos(), isMaster ? MASTER_COLOR : (isMain ? MAIN_COLOR : this.color),
                        "@%x".formatted(serverModule.ID), 2000);
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

    public void crash(Exception exception)
    {
        this.color = ERROR_COLOR;
        AmberCraft.LOGGER.error(exception.getMessage(), exception);
        errors.add(exception.getMessage());
    }

    public void warn(Exception exception)
    {
        this.color = errors.isEmpty() ? WARN_COLOR : ERROR_COLOR;//priority erros.
        AmberCraft.LOGGER.warn(exception.getMessage(), exception);
        warn.add(exception.getMessage());
    }

    public void checkConsistence()
    {
        if (serverModule.getNeighbors().length != childConnection.length)
            warn(new IllegalStateException("The number of children doesn't match the number of connections."));
        if (!serverModule.isRoot() && this.root == null)
            warn(new IllegalStateException("The widget not founded!"));
        RuntimeException[] exception = serverModule.checkInconsistencies();
        if (exception != null)
            for (RuntimeException e : exception)
                crash(e);
    }

    public void resolveCollision(NetworkWidget widget, Scheduler scheduler) {
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

    public void RENDER_TOOL_TIPS(GuiGraphics graphics, int mouseX, int mouseY, float ignoredParcialTick) {
        if (active == null)
            return;
        NetworkModule father = active.getRoot();
        boolean isMaster = active.getMaster() != null;

        ArrayList<Component> list = new ArrayList<>();
        list.add(Component.literal(active.getClass().getSimpleName()).withColor(DyeColor.WHITE.getTextColor()));
        for (var w : errors)
            list.add(Component.literal(w).withColor(NetworkWidget.ERROR_COLOR));
        for (var w : warn)
            list.add(Component.literal(w).withColor(NetworkWidget.WARN_COLOR));
        list.add(Component.literal("ID: " + Integer.toHexString(active.ID)).withColor(10494192));
        list.add(Component.literal("IsMaster: " + (isMaster ? "true " : "false")).withColor(10494192));
        list.add(Component.literal("Father: " + (father.isRoot() ? "root" : Integer.toHexString(father.ID))).withColor(10494192));
        list.add(Component.literal("Children: ").withColor(10494192));

        NetworkModule[] children = active.getNeighbors();
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

        if (active instanceof DebugToolInfo info)
            list.addAll(info.getInfo());

        var last = graphics.pose().last().copy().pose();
        graphics.pose().popPose();
        graphics.renderComponentTooltip(Minecraft.getInstance().font,
                list,
                mouseX, mouseY);
        graphics.pose().pushTransformation(new Transformation(last));
    }

    public void RENDER_FATHER_ARROW(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        if (root == null)
            return;
        new Connection(this, root, FATHER_CONNECTION_COLOR).render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public void setAnimation(Scheduler scheduler, Animation animation) {
        if (this.animation == null)
        {
            this.animation = animation;
            scheduler.add(animation);
        } else if (this.animation.isDone())
        {
            scheduler.add(animation);
            this.animation = animation;
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
