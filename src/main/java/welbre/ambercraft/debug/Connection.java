package welbre.ambercraft.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

public record Connection(ScreenNode father, ScreenNode child, int color) implements Renderable {
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        drawLine(guiGraphics, father.getCenter()[0], father.getCenter()[1], child.getCenter()[0], child.getCenter()[1], color);

        guiGraphics.drawSpecial(
                multiBufferSource -> {
                    VertexConsumer buffer = multiBufferSource.getBuffer(RenderType.DEBUG_TRIANGLE_FAN);
                    int[] center = father.getCenter();
                    int[] center_b = child.getCenter();
                    double length = Math.sqrt(
                            Math.pow(center_b[0] - center[0], 2) + Math.pow(center_b[1] - center[1], 2)
                    );
                    double[] dir = {(center_b[0] - center[0]) / length, (center_b[1] - center[1]) / length};

                    double angle = Math.atan2(center_b[1] - center[1], center_b[0] - center[0]);
                    double dist = Math.min(30.0 / Math.abs(Math.cos(angle)), 30.0 / Math.abs(Math.sin(angle)));
                    angle += Math.PI * 3.0 / 4.0;

                    int avgX = (int) Math.floor(center[0] + dir[0] * (length - dist) + 0.5);
                    int avgY = (int) Math.floor(center[1] + dir[1] * (length - dist) + 0.5);
                    //System.out.println(angle);

                    var poseStack = guiGraphics.pose();
                    poseStack.pushPose();

                    poseStack.rotateAround(new Quaternionf().rotateZ((float) angle), avgX, avgY, 0);
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
}
