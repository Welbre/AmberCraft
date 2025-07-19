package welbre.ambercraft.debug;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.heat.HeatModule;

import java.lang.reflect.Field;
import java.util.*;

public class NetworkScreen extends Screen {
    private final HeatModule main;
    private final List<ScreenNode> nodes = new ArrayList<>();
    private final List<Connection> connections = new ArrayList<>();

    public NetworkScreen(HeatModule main) {
        super(Component.literal("Network viewer").withColor(DyeColor.PURPLE.getTextColor()));
        this.main = main;
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
        if (father != null)
            this.addModule(father);

        @Nullable ScreenNode fatherNode = getScreenNode(father);

        //nextCenter()
        var center = nextCenter(fatherNode);
        var node = new ScreenNode(
                center[0],center[1],60,60,
                module == main ? 0xff7f00ff : 0xFFAE8094,
                module
        );
        nodes.add(node);

        HeatModule[] children = forcedGet(module, "children");
        if (children != null)
            for (HeatModule child : children)
            {
                this.addModule(child);

                ScreenNode childNode = getScreenNode(child);

                if (childNode != null)
                    connections.add(new Connection(node, childNode));
            }
    }

    @Override
    protected void init() {
        super.init();
        addModule(main.getRoot());
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
    }

    private int[] nextCenter(ScreenNode father)
    {
        final double offAngle = Math.toRadians(360D/4D);// 1/4 of a circle
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

    @Override
    public void tick() {
        super.tick();
//        double[] center = new double[]{this.width / 2d, this.height / 2d};
//        for (ScreenNode node : nodes)
//        {
//            double[] direction = new double[]{center[0] - node.x, center[1] - node.y};
//            double distance = Math.sqrt(direction[0]*direction[0] + direction[1]*direction[1]);
//            direction[0] /= distance;
//            direction[1] /= distance;
//
//            node.x += (int)(direction[0] * 1.5);
//            node.y += (int)(direction[1] * 1.5);
//        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        for (Connection connection : connections) connection.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.flush();

        for (ScreenNode node : nodes)
        {
            node.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    public static void drawLine(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;

        int err = dx - dy;

        while (true) {
            guiGraphics.fill(x1, y1, x1 + 1, y1 + 1, color); // desenha 1 pixel

            if (x1 == x2 && y1 == y2) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    public record Connection(ScreenNode a, ScreenNode b){
        public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
        {
            drawLine(guiGraphics, a.getCenter()[0], a.getCenter()[1], b.getCenter()[0], b.getCenter()[1], 0xFF0000FF);
        }
    }

    public static class ScreenNode {
        public int x;
        public int y;
        public int width;
        public int height;
        public int backGround;
        public HeatModule module;

        public ScreenNode(int x, int y, int width, int height, int backGround, HeatModule module) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.backGround = backGround;
            this.module = module;
        }

        public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
        {
            guiGraphics.fill(x, y, x + width, y + height, backGround);
            if ((boolean) forcedGet(module, "isMaster"))
                guiGraphics.fill(x+width-5,y+width-5, x + width, y + height,0xffff0000);

            guiGraphics.drawString(Minecraft.getInstance().font, "@"+Integer.toHexString(module.ID), x, y, 0xFFFFFFFF);
        }

        public int[] getCenter() {
            return new int[]{(int) Math.floor((x + width / 2.0) + 0.5), (int) Math.floor((y + height / 2.0))};
        }
    }

    private static <T> T forcedGet(Object object, String fieldName) {
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
            AmberCraft.LOGGER.error(e.getMessage());
        }
        return null;
    }
}
