package welbre.ambercraft.debug.network;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.network.NetworkModule;
import welbre.ambercraft.network.NetworkViewerScreenPayLoad;

import java.util.*;

//todo render the block in the screen based on the modulesholder
//todo create a button to toggle the ModulesHolder packer, a function to create one widget to holders that contains more that 1 module.
public class NetworkViewerScreen extends Screen {

    //navigation
    public PoseStack navigation = new PoseStack();
    public Matrix4f inverse = new Matrix4f();
    public PoseStack zoomStack = new PoseStack();
    public float zoom = 1f;
    public Vec2 position = new Vec2(0,0);
    public PoseStack rotationStack = new PoseStack();
    public Vec2 rotationPoint = new Vec2(0,0);

    //interation
    public NetworkWidget dragging = null;
    public Vec2 drawStart = null;
    /// -1 is used to show all layers
    public int layer = 0;
    public boolean showingServerModules = true;
    //interation->GUI
    /// Ignore the navigation and is rendered in the last step to be above other elements.
    public final List<AbstractWidget> fixedRenderableWidget = new ArrayList<>();

    //logical
    public final BlockPos clickedBlock;
    /// widgets sorted by layer
    public final List<List<NetworkWidget>> networkWidgets = new ArrayList<>();
    public final NetworkModule[] serverModules;
    public final Scheduler scheduler = new Scheduler();
    public final Scheduler renderScheduler = new Scheduler();

    public NetworkViewerScreen(FriendlyByteBuf buf) {
        super(Component.literal("Network Viewer"));
        this.clickedBlock = buf.readBlockPos();//first read the blockPos!
        this.serverModules = NetworkViewerScreenPayLoad.ModulesFromData(buf.readByteArray());
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int x, int y, float partialTick) {
        graphics.pose().pushTransformation(new Transformation(navigation.last().pose()));

        renderScheduler.tick(0.05f * partialTick);
        super.render(graphics, x, y, partialTick);

        graphics.pose().popPose();

        for (AbstractWidget widget : fixedRenderableWidget)
            widget.render(graphics, x, y, partialTick);
    }

    @Override
    protected void init() {
        //this function is called when a player opens the screen, and when a screen resizes.
        networkWidgets.clear();
        fixedRenderableWidget.clear();
        scheduler.clear();
        renderScheduler.clear();
        clearWidgets();

        //populate the widgets.
        List<NetworkWidget> widgetList = NetworkViewerHelper.CREATE_ALL_WIDGETS(clickedBlock, serverModules);
        for (NetworkWidget networkWidget : widgetList)
        {
            networkWidget.setPosition(new Random().nextInt(width), new Random().nextInt(height));
            addRenderableWidget(networkWidget);
        }

        var layers = NetworkViewerHelper.SORT_LAYERS(serverModules, widgetList);
        initLayersColors(layers);
        networkWidgets.addAll(layers);//if the list isn't clean, this will break!

        setLayer(0);
        InitButtons();
    }

    @Override
    public void tick() {
        super.tick();
        List<NetworkWidget> selected = getVisibleWidgets();
        final int size = selected.size();
        for (int i = 0; i < size; i++)
        {
            NetworkWidget widget = selected.get(i);
            for (int j = i+1; j < size; j++)
            {
                widget.resolveCollision(selected.get(j), renderScheduler);
            }
        }
        scheduler.tick(0.05f);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        var vec = TransformGlobalMouseToLocal(mouseX, mouseY);
        fixedRenderableWidget.forEach(widget -> widget.mouseMoved(mouseX, mouseY));

        super.mouseMoved(vec.x, vec.y);
        getVisibleWidgets().forEach(width -> width.shouldRenderToolTip = width.isMouseOver(vec.x, vec.y));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        fixedRenderableWidget.forEach(widget -> widget.mouseClicked(mouseX, mouseY, button));
        var mouse = this.TransformGlobalMouseToLocal(mouseX, mouseY);

        if (button == 0)
        {
            for (NetworkWidget widget : getVisibleWidgets())
            {
                if (widget.visible && widget.isMouseOver(mouse.x, mouse.y))
                {
                    dragging = widget;
                    drawStart = new Vec2(mouse.x - widget.getX(), mouse.y - widget.getY());
                    break;
                }
            }
        }
        if (button == 2)
            rotationPoint = mouse;

        return super.mouseClicked(mouse.x, mouse.y, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        fixedRenderableWidget.forEach(widget -> widget.mouseDragged(mouseX, mouseY, button, dragX, dragY));
        var mouse = this.TransformGlobalMouseToLocal(mouseX, mouseY);

        if (button == 0)
        {
            if (dragging != null)
            {
                renderScheduler.removeTask(dragging.animation);
                dragging.animation = null;

                dragging.setPosition((int) (mouse.x - drawStart.x), (int) (mouse.y - drawStart.y));
                return true;
            }
        }

        //return if another object in the scene handles this event with success
        boolean result = super.mouseDragged(mouse.x, mouse.y, button, dragX, dragY);
        if (result)
            return true;

        // only reach this line if mouse dragged in the background
        if (button == 0)//move
        {
            position = position.add(new Vec2((float) dragX / zoom, (float) dragY / zoom));
            ComputeNavigation();
            return true;
        }
        if (button == 2)//rotation
        {
            rotationStack.pushPose();
            Matrix4f pose = rotationStack.last().pose();
            pose.translate(rotationPoint.x, rotationPoint.y, 0);
            pose.rotate((float) dragX / 100f, 0, 0, 1);
            pose.translate(-rotationPoint.x, -rotationPoint.y, 0);
            ComputeNavigation();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        fixedRenderableWidget.forEach(widget -> widget.mouseReleased(mouseX, mouseY, button));
        if (button == 0)
            dragging = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        fixedRenderableWidget.forEach(widget -> widget.mouseScrolled(mouseX, mouseY, scrollX, scrollY));
        var mouse = this.TransformGlobalMouseToLocal(mouseX, mouseY);
        final float newZoom = (float) Math.max(0.25, Math.min(4.0, zoom + scrollY * 0.25));
        final float delta = newZoom - zoom;
        if (delta == 0)
            return false;

        zoomStack.pushPose();
        zoomStack.last().pose().translate(mouse.x, mouse.y, 0);
        zoomStack.last().pose().scale(1f+delta, 1f+delta, 1);
        zoomStack.last().pose().translate(-mouse.x, -mouse.y, 0);
        ComputeNavigation();

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (getFocused() instanceof EditBox box)//if the edit box is on focus, handle and return to avoid other interactions
            return box.keyPressed(keyCode, scanCode, modifiers);

        if (keyCode == 32)// Space bar
        {
            centralize();
            return true;
        } else if (keyCode == 70 && modifiers == 2) //ctrl + f
        {
            // ctrl + f toggle the edit box, so if already create, remove it!
            boolean success = createSearchBox();
            if (!success)
                removeSearch();

            return true;
        }

        for (AbstractWidget widget : fixedRenderableWidget)
            if (widget.keyPressed(keyCode, scanCode, modifiers))
                return true;

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private EditBox getSearchBox()
    {
        for (GuiEventListener child : children())
        {
            if (child instanceof EditBox editBox)
                if (editBox.getMessage().equals(Component.literal("node search")))
                    return editBox;
        }
        return null;
    }

    /// @return if the box is created or already exits one.
    private boolean createSearchBox()
    {
        EditBox search = getSearchBox();
        if (search != null)
            return false;

        search = new EditBox(this.font, this.width /2 - 50, this.height/2 - 9, 100, 18, Component.literal("node search"));
        search.setResponder(this::SEARCH_BOX_RESPONDER);
        search.setCanLoseFocus(true);
        search.setFocused(true);
        this.setFocused(search);
        this.addRenderableWidget(search);

        return true;
    }

    private void removeSearch()
    {
        var editBox = getSearchBox();
        if (editBox != null)
        {
            removeWidget(editBox);
            setFocused(null);
        }
    }

    /// set the widget in the center
    public void centralize(NetworkWidget widget)
    {
        final int dx = (this.width/2) - widget.getCenter()[0];
        final int dy = (this.height/2) - widget.getCenter()[1];
        for (NetworkWidget n : getVisibleWidgets())
            scheduler.add(new Animation(n, dx + n.getX(), dy + n.getY(), .5f, Animation.Interpolations.EASE_OUT_QUART));
    }

    /// Find the geometric center of the network and move to it.
    public void centralize()
    {
        int x = 0;
        int y = 0;
        for (NetworkWidget node : getVisibleWidgets())
        {
            x += node.getX();
            y += node.getY();
        }
        x = (x / networkWidgets.size()) - (this.width / 2) + 30;
        y = (y / networkWidgets.size()) - (this.height / 2) + 30;

        position = new Vec2(0,0);
        zoomStack = new PoseStack();
        rotationStack = new PoseStack();
        zoom = 1f;

        ComputeNavigation();

        for (NetworkWidget node : getVisibleWidgets())
            scheduler.add(new Animation(node, node.getX() - x, node.getY() - y, .5f, Animation.Interpolations.EASE_OUT_QUART));
    }

    private void SEARCH_BOX_RESPONDER(String text) {
        if (text.startsWith("@"))
            text = text.substring(1);
        if (text.isEmpty())
            return;

        text = text.toLowerCase();
        for (NetworkWidget widget : getVisibleWidgets())
        {
            if (String.format("%x", widget.serverModule.ID).contains(text))
            {
                this.scheduler.clear(Animation.class);//remove all animations
                this.centralize(widget);
                if (String.format("%x", widget.serverModule.ID).equals(text))//remove if findit
                {
                    ADD_SEARCH_MARK(this, widget);
                    removeSearch();
                    return;
                }
            }
        }
    }

    private void ComputeNavigation()
    {
        navigation = new PoseStack();
        navigation.pushPose();
        navigation.last().pose().translation(position.x, position.y , 0);

        navigation.pushTransformation(new Transformation(zoomStack.last().copy().pose()));
        navigation.pushTransformation(new Transformation(rotationStack.last().copy().pose()));

        inverse = navigation.last().copy().pose().invert();
    }

    private Vec2 TransformGlobalMouseToLocal(double x, double y)
    {
        Vector3f pos = inverse.transformPosition(new Vector3f((float) x, (float) y, 0));

        return new Vec2(pos.x, pos.y);
    }

    private static void ADD_SEARCH_MARK(NetworkViewerScreen screen, NetworkWidget widget)
    {
        Renderable render = (guiGraphics, mouseX, mouseY, partialTick) -> guiGraphics.fill(
                widget.getX()-5, widget.getY()-5,
                widget.getX()+widget.getWidth()+5, widget.getY()+widget.getHeight()+5,
                0x8000FF00);
        screen.scheduler.schedule(50, task -> screen.renderables.remove(render));
        screen.addRenderableOnly(render);
    }

    private void InitButtons()
    {
        var radialTreeSort = new Button.Builder(Component.literal("Radial tree view"), this::handleRadialTreeSort).bounds(0,0,100,18).build();
        var layerButon = new Button.Builder(Component.literal("0"), this::handleLayer).bounds(width-18,0,18,18).build();
        var modulesSide = new Button.Builder(Component.literal("serverSide").withColor(DyeColor.LIGHT_BLUE.getTextColor()), this::handleModuleSide).bounds(width-18-60, 0, 60, 18).build();

        fixedRenderableWidget.add(radialTreeSort);
        fixedRenderableWidget.add(layerButon);
        fixedRenderableWidget.add(modulesSide);
    }

    private void handleRadialTreeSort(Button button)
    {
        renderScheduler.clear();

        // remove other sorts
        var toRemove = renderables.stream()
                .map(s -> s instanceof AbstractWidget widget ? widget : null)
                .filter(Objects::nonNull)
                .filter(s -> s.getMessage().equals(NetworkWidgetSorter.NAME))
                .toList();
        toRemove.forEach(renderables::remove);

        try
        {
            AbstractWidget result = new RadialTreeSorter().sort(button, this);
            if (result != null)
                renderables.addFirst(result);

        } catch (Exception e)
        {
            AmberCraft.LOGGER.error("Error while sorting the network!",e);
        }
    }

    private void handleLayer(Button button)
    {
        setLayer(layer + 1);
        if (layer == -1)
            button.setMessage(Component.literal("All"));
        else
            button.setMessage(Component.literal(String.valueOf(layer)));

        renderScheduler.clear();
    }

    private void handleModuleSide(Button button)
    {
        showingServerModules = !showingServerModules;
        if (showingServerModules)
        {
            button.setMessage(Component.literal("serverSide").withColor(DyeColor.LIGHT_BLUE.getTextColor()));
            networkWidgets.forEach(s -> s.forEach(w -> w.active = w.serverModule));
        }
        else
        {
            button.setMessage(Component.literal("clientSide").withColor(DyeColor.ORANGE.getTextColor()));
            networkWidgets.forEach(s -> s.forEach(w -> w.active = w.clientModule));
        }
    }

    public List<Renderable> temp = renderables;//todo remove it


    public void setLayer(int layer)
    {
        if (layer > networkWidgets.size()-1)
            //if only one layer is present,
            //don't need to go to the "all" layers state
            this.layer = networkWidgets.size() == 1 ? 0 : -1;
        else
            this.layer = layer;

        //disable all
        networkWidgets.forEach(list -> list.forEach(networkWidget -> networkWidget.visible = (this.layer == -1)));//-1 is used to show all layers
        //turn only layer on
        if (this.layer != -1)
            networkWidgets.get(this.layer).forEach(networkWidget -> networkWidget.visible = true);
    }

    public List<NetworkWidget> getVisibleWidgets()
    {
        ArrayList<NetworkWidget> result = new ArrayList<>();

        for (List<NetworkWidget> layer : networkWidgets)
            for (NetworkWidget widget : layer)
                if (widget.visible)
                    result.add(widget);

        return result;
    }

    /// Colorize the widgets based on the layer that they are inserted.
    private void initLayersColors(List<List<NetworkWidget>> layers) {
        int index = 0;
        for (int i = 1; i < layers.size(); i++)
        {
            if (index >= NetworkWidget.DEFAULT_EXTRA_COLORS.length)
                index = 0;

            var layer = layers.get(i);
            int color = NetworkWidget.DEFAULT_EXTRA_COLORS[index];
            for (NetworkWidget widget : layer)
                widget.color = color;

            index++;
        }
    }

    public static @Nullable NetworkWidget GET_WIDGET(List<NetworkWidget> widgets, NetworkModule module)
    {
        for (NetworkWidget widget : widgets)
            if (widget.serverModule.ID == module.ID)
                return widget;

        return null;
    }

    @Override
    protected void renderMenuBackground(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        var last = guiGraphics.pose().last().copy().pose();
        guiGraphics.pose().popPose();
        super.renderMenuBackground(guiGraphics, x,y, width, height);
        guiGraphics.pose().pushTransformation(new Transformation(last));
    }
}
