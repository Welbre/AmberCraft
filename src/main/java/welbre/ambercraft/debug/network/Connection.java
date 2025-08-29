package welbre.ambercraft.debug.network;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

public record Connection(NetworkWidget father, NetworkWidget child, int color) implements Renderable {
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        int[] center = father.getCenter();
        int[] center_b = child.getCenter();
        double length = Math.sqrt(
                Math.pow(center_b[0] - center[0], 2) + Math.pow(center_b[1] - center[1], 2)
        );
        double[] dir = {(center_b[0] - center[0]) / length, (center_b[1] - center[1]) / length};

        double angle = Math.atan2(center_b[1] - center[1], center_b[0] - center[0]);
        double dist = Math.min(30.0 / Math.abs(Math.cos(angle)), 30.0 / Math.abs(Math.sin(angle)));

        int[] edge = {child.getCenter()[0] - (int) (dir[0]*dist), child.getCenter()[1] - (int) (dir[1]*dist)};
        drawLine(guiGraphics, father.getCenter()[0], father.getCenter()[1], edge[0], edge[1], color);

        guiGraphics.drawSpecial(
                multiBufferSource -> {
                    VertexConsumer buffer = multiBufferSource.getBuffer(RenderType.DEBUG_TRIANGLE_FAN);
                    double angle0 = angle + Math.PI * 3.0 / 4.0;

                    int avgX = (int) Math.floor(center[0] + dir[0] * (length - dist) + 0.5);
                    int avgY = (int) Math.floor(center[1] + dir[1] * (length - dist) + 0.5);
                    //System.out.println(angle);

                    var poseStack = guiGraphics.pose();
                    poseStack.pushPose();

                    poseStack.rotateAround(new Quaternionf().rotateZ((float) angle0), avgX, avgY, 0);
                    buffer.addVertex(poseStack.last(), avgX, avgY, 0).setColor(color);
                    buffer.addVertex(poseStack.last(), avgX + 10, avgY, 0).setColor(color);
                    buffer.addVertex(poseStack.last(), avgX, 10 + avgY, 0).setColor(color);

                    poseStack.popPose();
                }
        );
    }

    public static void drawLine(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        if (dx > 4000 || dy > 4000) return;

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

    public static void drawLineAA(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        // Extrai componentes RGBA
        int r = (color >> 16) & 0xFF;
        int gC = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (color >> 24) & 0xFF;

        boolean steep = Math.abs(y2 - y1) > Math.abs(x2 - x1);
        if (steep) {
            // Troca x e y
            int tmp = x1; x1 = y1; y1 = tmp;
            tmp = x2; x2 = y2; y2 = tmp;
        }
        if (x1 > x2) {
            int tmp = x1; x1 = x2; x2 = tmp;
            tmp = y1; y1 = y2; y2 = tmp;
        }

        int dx = x2 - x1;
        int dy = y2 - y1;
        double gradient = (dx == 0) ? 1.0 : (double) dy / dx;

        // primeira extremidade
        int xend = x1;
        double yend = y1 + gradient * (xend - x1);
        double xgap = rfpart(x1 + 0.5);
        int xpxl1 = xend;
        int ypxl1 = ipart(yend);

        if (steep) {
            plot(g, ypxl1,   xpxl1, r, gC, b, a, rfpart(yend) * xgap);
            plot(g, ypxl1+1, xpxl1, r, gC, b, a, fpart(yend) * xgap);
        } else {
            plot(g, xpxl1, ypxl1,   r, gC, b, a, rfpart(yend) * xgap);
            plot(g, xpxl1, ypxl1+1, r, gC, b, a, fpart(yend) * xgap);
        }
        double intery = yend + gradient;

        // segunda extremidade
        xend = x2;
        yend = y2 + gradient * (xend - x2);
        xgap = fpart(x2 + 0.5);
        int xpxl2 = xend;
        int ypxl2 = ipart(yend);

        if (steep) {
            plot(g, ypxl2,   xpxl2, r, gC, b, a, rfpart(yend) * xgap);
            plot(g, ypxl2+1, xpxl2, r, gC, b, a, fpart(yend) * xgap);
        } else {
            plot(g, xpxl2, ypxl2,   r, gC, b, a, rfpart(yend) * xgap);
            plot(g, xpxl2, ypxl2+1, r, gC, b, a, fpart(yend) * xgap);
        }

        // loop
        if (steep) {
            for (int x = xpxl1 + 1; x < xpxl2; x++) {
                plot(g, ipart(intery),   x, r, gC, b, a, rfpart(intery));
                plot(g, ipart(intery)+1, x, r, gC, b, a, fpart(intery));
                intery += gradient;
            }
        } else {
            for (int x = xpxl1 + 1; x < xpxl2; x++) {
                plot(g, x, ipart(intery),   r, gC, b, a, rfpart(intery));
                plot(g, x, ipart(intery)+1, r, gC, b, a, fpart(intery));
                intery += gradient;
            }
        }
    }

    // helpers
    private static int ipart(double x) { return (int) Math.floor(x); }
    private static double fpart(double x) { return x - Math.floor(x); }
    private static double rfpart(double x) { return 1.0 - fpart(x); }

    private static void plot(GuiGraphics g, int x, int y, int r, int gg, int b, int a, double brightness) {
        int alpha = (int) (a * brightness);
        if (alpha <= 0) return;
        int color = (alpha << 24) | (r << 16) | (gg << 8) | b;
        g.fill(x, y, x+1, y+1, color);
    }
}
