package welbre.ambercraft.debug;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.module.heat.HeatModule;

import java.util.List;

public class ScreenNode {
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

    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(x, y, x + width, y + height, backGround);
        if ((boolean) NetworkScreen.forcedGet(module, "isMaster"))
            guiGraphics.fill(x + width - 5, y + width - 5, x + width, y + height, 0xffff0000);

        guiGraphics.drawString(Minecraft.getInstance().font, "@" + Integer.toHexString(module.ID), x, y, 0xFFFFFFFF);
    }

    public int[] getCenter() {
        return new int[]{(int) Math.floor((x + width / 2.0) + 0.5), (int) Math.floor((y + height / 2.0))};
    }

    public boolean areNodesColliding(ScreenNode node2) {
        return this.x < node2.x + node2.width &&
                this.x + this.width > node2.x &&
                this.y < node2.y + node2.height &&
                this.y + this.height > node2.y;
    }

    public void resolveCollision(ScreenNode node2, List<Animation> animations) {
        // Calculate center points
        float center1X = this.x + this.width / 2f;
        float center1Y = this.y + this.height / 2f;
        float center2X = node2.x + node2.width / 2f;
        float center2Y = node2.y + node2.height / 2f;

        // Calculate direction vector
        double dirX = center2X - center1X;
        double dirY = center2Y - center1Y;

        // Normalize direction
        double length = Math.sqrt(dirX * dirX + dirY * dirY);
        if (length == 0)
        {
            dirX = 1;
            dirY = 1;
            length = 1;
        }
        if (length > 0)
        {
            dirX /= length;
            dirY /= length;

            animations.add(new Animation(this, this.x - (int) (dirX * 10), this.y - (int) (dirY * 10), 10));
            animations.add(new Animation(node2, node2.x + (int) (dirX * 10), node2.y + (int) (dirY * 10), 10));
        }
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
    }
}
