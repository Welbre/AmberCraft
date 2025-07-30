package welbre.ambercraft.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.GameTestAddMarkerDebugPayload;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.heat.HeatModule;
import welbre.ambercraft.module.network.NetworkModule;
import welbre.ambercraft.sim.heat.HeatNode;

import java.lang.reflect.Field;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public class NetworkScreen extends Screen {
    private final PoseStack poseStack = new PoseStack();
    private double zoomLevel = 1.0;

    private ScreenNode selectedNode = null;
    private int dragStartX;
    private int dragStartY;

    final NetworkModule main;
    final NetworkWrapperModule<?> wrapper;
    final List<ScreenNode> nodes = new ArrayList<>();
    final List<Connection> connections = new ArrayList<>();
    final List<Animation> animations = new ArrayList<>();

    public NetworkScreen(NetworkWrapperModule<?> main) {
        super(Component.literal("Network viewer").withColor(DyeColor.PURPLE.getTextColor()));
        this.main = main.getModule()[0];
        this.wrapper = main;
    }


    ScreenNode getScreenNode(NetworkModule module)
    {
        for (ScreenNode screenNode : nodes)
            if (screenNode.module == module)
                return screenNode;

        return null;
    }

    private void addModule(NetworkModule module)
    {
        if (getScreenNode(module) != null)
            return;

        NetworkModule father = module.getFather();

        var node = new ScreenNode(
                0,0,60,60,
                module == main ? 0xff7f00ff : 0xFFAE8094,
                module
        );
        nodes.add(node);

        if (father != null)
            this.addModule(father);

        @Nullable ScreenNode fatherNode = getScreenNode(father);

        var pos = nextCenter(fatherNode);
        //var pos = nextGrid(5);
        node.x = pos[0];
        node.y = pos[1];

        RuntimeException ok = module.checkInconsistencies();
        if (ok != null)
            node.applyError(ok);

        NetworkModule[] children = module.getChildren();
        if (children != null)
            for (NetworkModule child : children)
            {
                this.addModule(child);

                ScreenNode childNode = getScreenNode(child);

                if (childNode != null)
                    connections.add(new Connection(node, childNode, 0xFF0000FF));
            }
    }

    @Override
    protected void init() {
        super.init();
        addModule(main);
        ScreenNode screenNode = getScreenNode(main);

        if (screenNode == null)
            return;
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int[] dir = {centerX - screenNode.x, centerY - screenNode.y};
        for (ScreenNode node : nodes)
        {
            node.x += dir[0];
            node.y += dir[1];
        }

        checkCyclical();

        for (ScreenNode node : this.nodes)
            node.setWorldPos(wrapper.findBlockEntity(node.module));


        this.addRenderableWidget(Button.builder(Component.literal("Tree viewer"), (a) -> TreeViewerSort.sort(this)).pos(0,0).size(100,18).build());
        this.addRenderableWidget(Button.builder(Component.literal("Orbital viewer"), (a) -> OrbitalViewerSort.sort(this)).pos(100,0).size(100,18).build());
        this.addRenderableWidget(Button.builder(Component.literal("Onion viewer"), (a) -> OnionViewerSort.sort(this)).pos(200,0).size(100,18).build());
    }

    @Override
    public void tick() {
        super.tick();

        // Check and resolve collisions between all pairs of nodes
        for (int i = 0; i < nodes.size(); i++)
        {
            for (int j = i + 1; j < nodes.size(); j++)
            {
                ScreenNode node1 = nodes.get(i);
                ScreenNode node2 = nodes.get(j);

                if (node1.areNodesColliding(node2))
                    node1.resolveCollision(node2,animations);
            }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().pushTransformation(new Transformation(poseStack.last().pose()));
        var transformed = poseStack.last().copy().pose().invert().transformPosition(new Vector3f((float)mouseX, (float)mouseY, 0));
        mouseX = (int) transformed.x;
        mouseY = (int) transformed.y;

        for (Animation animation : animations)
            animation.tick(partialTick);
        animations.removeIf(Animation::done);

        for (Connection connection : connections) connection.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.flush();

        for (ScreenNode node : nodes)
        {
            if (node.isMouseOver(mouseX, mouseY))
            {
                RENDER_TOOL_TIPS(guiGraphics, mouseX, mouseY, partialTick, node);
                ScreenNode nFather = getScreenNode(node.module.getFather());
                if (nFather != null)
                    new Connection(node, nFather, 0xFFCC0000).render(guiGraphics, mouseX, mouseY, partialTick);
            }
            node.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        guiGraphics.pose().popPose();

        for (Renderable renderable : this.renderables)
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // Add these methods to handle mouse interactions
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        var transformed = poseStack.last().copy().pose().invert().transformPosition(new Vector3f((float)mouseX, (float)mouseY, 0));
        mouseX = transformed.x;
        mouseY = transformed.y;

        if (button == 0) { // Left click
            for (ScreenNode node : nodes) {
                if (node.isMouseOver((int)mouseX, (int)mouseY)) {
                    selectedNode = node;
                    dragStartX = (int)mouseX - node.x;
                    dragStartY = (int)mouseY - node.y;
                    BlockEntity entity = wrapper.findBlockEntity(node.module);
                    if (entity != null)
                    {
                        boolean isMaster = node.module.getMaster() != null;
                        PacketDistributor.sendToAllPlayers(
                                new GameTestAddMarkerDebugPayload(entity.getBlockPos(), isMaster ? 0xcc880000: ( node.module ==  this.main ? 0xdd7f00ff : 0xccFFFFFF), "@%x".formatted(node.module.ID), 1500)
                        );
                    }
                    return true;
                }
            }

        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        var transformed = poseStack.last().copy().pose().invert().transformPosition(new Vector3f((float)mouseX, (float)mouseY, 0));
        mouseX = transformed.x;
        mouseY = transformed.y;

        if (button == 0) {
            if (selectedNode != null)
            {
                selectedNode.x = (int) mouseX - dragStartX;
                selectedNode.y = (int) mouseY - dragStartY;
                return true;
            }
            else
            {
                for (ScreenNode node : nodes) {
                    node.x += (int) dragX;
                    node.y += (int) dragY;
                }
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0)
            selectedNode = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        zoomLevel = Math.max(0.25, Math.min(4.0, zoomLevel + scrollY * 0.25));
        if (!poseStack.clear())
            poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(width / 2f, height / 2f, 0);
        poseStack.scale((float) zoomLevel, (float) zoomLevel, 1.0f);
        poseStack.translate(-width / 2f, -height / 2f, 0);
        return true;
    }

    public void centralize(ScreenNode node)
    {
        int x = (this.width/2) - node.x;
        int y = (this.height/2) - node.y;
        for (ScreenNode n : nodes)
            animations.add(new Animation(n, n.x + x, n.y + y, 20, Animation.Interpolations.EASE_OUT_QUART));
    }

    public void centralize()
    {
        int x = 0;
        int y = 0;
        for (ScreenNode node : nodes)
        {
            x += node.x;
            y += node.y;
        }
        x /= nodes.size();
        y /= nodes.size();

        x = x - (this.width / 2);
        y = y - (this.height / 2);

        for (ScreenNode node : nodes)
            animations.add(new Animation(node, node.x - x, node.y - y, 10, Animation.Interpolations.EASE_OUT_QUART));
    }


    public static EditBox SEARCH;
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 32)// Space bar
        {
            centralize();
            return true;
        } else if (keyCode == 70 && modifiers == 2) //ctrl + f
        {
            if (SEARCH != null)
            {
                this.removeWidget(SEARCH);
                SEARCH = null;
                return true;
            }

            SEARCH = new EditBox(this.font, this.width /2 - 50, this.height/2 - 9, 100, 18, Component.literal("Search"));
            SEARCH.setResponder((text) -> {
                if (text.startsWith("@"))
                    text = text.substring(1);
                if (text.isEmpty())
                    return;


                text = text.toLowerCase();
                for (var node : nodes)
                {
                    if (String.format("%x",node.module.ID).contains(text))
                    {
                        this.centralize(node);
                        if (String.format("%x",node.module.ID).equals(text))//remove if findit
                        {
                            this.removeWidget(SEARCH);
                            SEARCH = null;
                        }

                        return;
                    }
                }
            });
            SEARCH.setCanLoseFocus(true);
            SEARCH.setFocused(true);
            this.setFocused(SEARCH);
            this.addRenderableWidget(SEARCH);
            return true;
        }
        if (SEARCH != null)
            return SEARCH.keyPressed(keyCode, scanCode, modifiers);

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void checkCyclical() {
        //check cyclical connection.
        NetworkModule root;
        {
            Set<NetworkModule> visited = new HashSet<>();
            root = this.main;
            NetworkModule temp = this.main.getFather();

            while (temp != null)
            {
                if (visited.contains(temp))
                {
                    ScreenNode screen = getScreenNode(temp);
                    temp = null;
                    if (screen != null)
                        screen.applyError(new IllegalStateException("Cyclical connection detected on %x!".formatted(screen.module.ID)));
                    else
                        AmberCraft.LOGGER.warn("Cyclical connection detected on Null!");
                }
                visited.add(temp);
                root = temp;
                temp = temp == null ? null : temp.getFather();
            }
        }
    }

    private int[] nextCenter(ScreenNode father)
    {
        final double offAngle = Math.toRadians(360D/4D);// 1/4 of father circle
        final double range = 140;

        if (father== null)
            return new int[]{this.width / 2 -30, this.height / 2 -30};
        if (nodes.size() == 1)
        {
            final double angle = new Random().nextDouble(0D, Math.PI * 2D);
            return new int[]{
                    this.width / 2 - 30 + (int) (range * Math.cos(angle)),
                    this.height / 2 - 30 + (int) (range * Math.sin(angle))
            };
        }

        double[] offDir = new double[]{0D,0D};
        double weigh = 0;

        //average
        for (ScreenNode node : nodes)
        {
            if (node == father)
                continue;
            double localWeigh = 100.0 / Math.sqrt(Math.pow(node.x - father.x, 2) + Math.pow(node.y - father.y, 2));
            if (Double.isFinite(localWeigh))
            {
                weigh += localWeigh;
                offDir[0] += (father.x - node.x) * localWeigh;
                offDir[1] += (father.y - node.y) * localWeigh;
            }
        }
        offDir[0] /= weigh;
        offDir[1] /= weigh;
        //normalize
        double length = Math.sqrt(offDir[0] * offDir[0] + offDir[1] * offDir[1]);
        offDir[0] = (offDir[0] / length);
        offDir[1] = (offDir[1] / length);

        double dirAngle = Math.atan2(offDir[1],offDir[0]);
        if (Double.isNaN(dirAngle))
            return new int[]{0,0};

        Random random = new Random(father.hashCode());
        final double angle = random.nextDouble(
                dirAngle - (offAngle / 2.0),
                dirAngle + (offAngle / 2.0)
        );
        return new int[]{
                (int) (range * Math.cos(angle)) + father.x,
                (int) (range * Math.sin(angle)) + father.y
        };
    }

    public void RENDER_TOOL_TIPS(GuiGraphics graphics, int mouseX, int mouseY, float parcialTick, ScreenNode node){
        NetworkModule father = node.module.getFather();
        boolean isMaster = node.module.getMaster() != null;
        HeatNode heatNode = node.module instanceof HeatModule heat? heat.getHeatNode() : null;

        ArrayList<Component> list = new ArrayList<>(List.of(
                Component.literal("ID: " + Integer.toHexString(node.module.ID)).withColor(10494192),
                Component.literal("IsMaster: " + (isMaster ? "true " : "false")).withColor(10494192),
                Component.literal("Father: " + (father == null ? "root" : Integer.toHexString(father.ID))).withColor(10494192),
                Component.literal("Children: ").withColor(10494192)

        ));
        NetworkModule[] children = node.module.getChildren();
        for (NetworkModule module : children)
            list.add(Component.literal("-->Child: " + Integer.toHexString(module.ID)).withColor(10494192));

        BlockEntity entity = this.wrapper.findBlockEntity(node.module);
        if (entity != null)
            list.add(Component.literal("Pos: x=%d, y=%d, z=%d".formatted(
                    entity.getBlockPos().getX(),
                    entity.getBlockPos().getY(),
                    entity.getBlockPos().getZ()
            )).withColor(DyeColor.LIME.getTextColor()));
        else
            list.add(Component.literal("Pos: null").withColor(DyeColor.LIME.getTextColor()));

        if (heatNode != null)
        {
            list.add(Component.literal("Temperature: %.2fºC".formatted(heatNode.getTemperature())));
            list.add(Component.literal("Conductivity: %.2f W/ºC".formatted(heatNode.getThermalConductivity())));
            list.add(Component.literal("Capacidade: %.2f J/ºC".formatted(heatNode.getThermalMass())));
        } else {
            list.add(Component.literal("Heat not is null!").withColor(DyeColor.RED.getTextColor()));
        }

        graphics.renderComponentTooltip(Minecraft.getInstance().font,
                list,
                mouseX, mouseY);
    }
}
