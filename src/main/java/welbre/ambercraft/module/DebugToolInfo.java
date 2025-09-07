package welbre.ambercraft.module;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Uses exclusively by {@link welbre.ambercraft.debug.network.NetworkWidget} in {@link welbre.ambercraft.debug.network.NetworkWidget#RENDER_TOOL_TIPS(GuiGraphics, int, int, float)}
 * to show extra information about a module.
 */
@FunctionalInterface
public interface DebugToolInfo {
    List<Component> getInfo();
}
