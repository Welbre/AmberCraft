package welbre.ambercraft.debug;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.module.heat.HeatModule;

import java.lang.reflect.Field;
import java.util.*;

public class NetworkScreen extends Screen {
    private final HeatModule module;
    private final List<ScreenNode> nodes = new ArrayList<>();

    public NetworkScreen(HeatModule module) {
        super(Component.literal("Network viewer").withColor(DyeColor.PURPLE.getTextColor()));
        this.module = module;
    }

    @Override
    protected void init() {
        super.init();
        Random random = new Random();
        Queue<HeatModule> queue = new ArrayDeque<>(List.of(module.getRoot()));
        while (!queue.isEmpty())
        {
            HeatModule current = queue.poll();
            ScreenNode node = new ScreenNode(random.nextInt(width-60), random.nextInt(height-60), 60, 60,
                    (current == module ? 0xff7f00ff : 0xFFAE8094),
                    current);
            nodes.add(node);
            try
            {
                Field children = HeatModule.class.getDeclaredField("children");
                children.setAccessible(true);
                queue.addAll(Arrays.asList((HeatModule[]) children.get(current)));
                children.setAccessible(false);
            } catch (Exception ignored){}
        }
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        for (ScreenNode node : nodes)
        {
            node.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        // Renderiza fundo

        // Exemplo: desenha um retângulo vermelho
        //guiGraphics.fill(50, 50, 150, 150, 0xFFFF0000); // RGBA

        // Exemplo: desenha um texto
        //guiGraphics.drawString(this.font, "@"+Integer.toHexString(module.hashCode()), 60, 60, 0xFFFFFF);

        // Exemplo: linha (não tem nativa, mas você pode simular com fill)
        //guiGraphics.fill(160, 50, 161, 150, 0xFF00FF00); // linha verde

        //this.renderBackground(guiGraphics,mouseX,mouseY,partialTick);

        // Chama render padrão (como botões, etc)
        //super.render(guiGraphics, mouseX, mouseY, partialTick);
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
            drawLine(guiGraphics, a.x, a.y, b.x, b.y, 0xFF0000FF);
        }
    }

    public static class ScreenNode {
        public int x;
        public int y;
        public int width;
        public int height;
        public int backGround;
        public HeatModule module;
        public List<Connection> connectionList = new ArrayList<>();

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
            guiGraphics.drawString(Minecraft.getInstance().font, "@"+Integer.toHexString(module.ID), x, y, 0xFFFFFFFF);
            for (Connection connection : connectionList) {
                connection.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }
    }
}
