package welbre.ambercraft.debug;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.GameTestAddMarkerDebugPayload;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.heat.HeatModule;

import java.lang.reflect.Field;
import java.util.*;

public class NetworkScreen extends Screen {
    private static final int ERROR_COLOR = 0xFFDD0A0A;


    private final HeatModule main;
    private final NetworkWrapperModule wrapper;
    private final List<ScreenNode> nodes = new ArrayList<>();
    private final List<Connection> connections = new ArrayList<>();
    private final List<Animation> animations = new ArrayList<>();

    public NetworkScreen(NetworkWrapperModule main) {
        super(Component.literal("Network viewer").withColor(DyeColor.PURPLE.getTextColor()));
        this.main = main.getModule();
        this.wrapper = main;
    }


    private ScreenNode getScreenNode(HeatModule module)
    {
        for (ScreenNode screenNode : nodes)
            if (screenNode.module == module)
                return screenNode;

        return null;
    }

    private void addModule(HeatModule module)
    {
        if (getScreenNode(module) != null)
            return;

        HeatModule father = forcedGet(module, "father");

        var node = new ScreenNode(
                0,0,60,60,
                module == main ? 0xff7f00ff : 0xFFAE8094,
                module
        );
        nodes.add(node);

        if (father != null)
            this.addModule(father);

        @Nullable ScreenNode fatherNode = getScreenNode(father);

        var center = nextCenter(fatherNode);
        node.x = center[0];
        node.y = center[1];


        //todo place in father function
        //check inconsistencies
        if (father != null){
            HeatModule[] children = forcedGet(father, "children");
            boolean isOk = false;
            for (HeatModule child : children)
                if (child == module)
                {isOk = true; break;}
            if (!isOk)
            {
                node.backGround = ERROR_COLOR;
                AmberCraft.LOGGER.error("Inconsistent network structure");
            }
        }

        HeatModule[] children = forcedGet(module, "children");
        if (children != null)
            for (HeatModule child : children)
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
                ScreenNode nFather = getScreenNode(forcedGet(node.module, "father"));
                if (nFather != null)
                    new Connection(node, nFather, 0xFFCC0000).render(guiGraphics, mouseX, mouseY, partialTick);
            }
            node.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private ScreenNode selectedNode = null;
    private int dragStartX;
    private int dragStartY;

    // Add these methods to handle mouse interactions
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            for (ScreenNode node : nodes) {
                if (node.isMouseOver((int)mouseX, (int)mouseY)) {
                    selectedNode = node;
                    dragStartX = (int)mouseX - node.x;
                    dragStartY = (int)mouseY - node.y;
                    BlockEntity entity = wrapper.findBlockEntity(node.module);
                    if (entity != null)
                    {
                        boolean isMaster = forcedGet(node.module, "isMaster");
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

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 32)// Space bar
        {
            centralize();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void checkCyclical() {
        //check cyclical connection.
        {
            Set<HeatModule> visited = new HashSet<>();
            Queue<HeatModule> search = new ArrayDeque<>(List.of(main));
            while (!search.isEmpty())
            {
                HeatModule module = search.poll();
                if (visited.contains(module))
                {
                    HeatModule[] children = forcedGet(module, "children");
                    for (HeatModule child : children)
                    {
                        if (visited.contains(child))
                        {
                            ScreenNode screen = getScreenNode(module);
                            if (screen != null)
                            {
                                screen.backGround = ERROR_COLOR;
                                AmberCraft.LOGGER.warn("Cyclical connection detected on %x!".formatted(screen.module.ID));
                            } else {
                                AmberCraft.LOGGER.warn("Cyclical connection detected on Null!");
                            }
                            break;
                        }
                    }

                    continue;
                }
                visited.add(module);

                HeatModule[] children = forcedGet(module, "children");
                if (children != null)
                    search.addAll(Arrays.asList(children));
                HeatModule root = forcedGet(module, "root");
                if (root != null)
                    search.add(root);
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

    public static <T> T forcedGet(Object object, String fieldName) {
        if (object == null)
            return null;
        try
        {
            Field declaredField = object.getClass().getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            //noinspection unchecked
            T filed = (T) declaredField.get(object);
            declaredField.setAccessible(false);
            return filed;
        } catch (Exception e)
        {
            AmberCraft.LOGGER.error("don't contains field " + e.getMessage());
        }
        return null;
    }

    public static void RENDER_TOOL_TIPS(GuiGraphics graphics, int mouseX, int mouseY, float parcialTick, ScreenNode node){
        HeatModule father = forcedGet(node.module, "father");
        boolean isMaster = forcedGet(node.module, "isMaster");
        ArrayList<Component> list = new ArrayList<>(List.of(
                Component.literal("ID: " + Integer.toHexString(node.module.ID)).withColor(10494192),
                Component.literal("IsMaster: " + (isMaster ? "true " : "false")).withColor(10494192),
                Component.literal("Father: " + (father == null ? "root" : Integer.toHexString(father.ID))).withColor(10494192),
                Component.literal("Children: ").withColor(10494192),
                Component.literal("Temperature: %.2fºC".formatted(node.module.getHeatNode().getTemperature())),
                Component.literal("Conductivity: %.2f W/ºC".formatted(node.module.getHeatNode().getThermalConductivity())),
                Component.literal("Capacidade: %.2f J/ºC".formatted(node.module.getHeatNode().getThermalMass()))
        ));
        HeatModule[] children = forcedGet(node.module, "children");
        for (HeatModule module : children)
        {
            list.add(3, Component.literal("-->Child: " + Integer.toHexString(module.ID)).withColor(10494192));
        }

        graphics.renderComponentTooltip(Minecraft.getInstance().font,
                list,
                mouseX, mouseY);
    }
}
