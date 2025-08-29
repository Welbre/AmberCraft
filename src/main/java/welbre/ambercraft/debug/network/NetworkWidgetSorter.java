package welbre.ambercraft.debug.network;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public interface NetworkWidgetSorter {
    AbstractWidget sort(Button button, NetworkViewerScreen screen);

    Component NAME = Component.literal("Sort");
}
