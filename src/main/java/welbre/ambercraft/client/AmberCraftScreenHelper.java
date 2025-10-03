package welbre.ambercraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import welbre.ambercraft.client.screen.HeatSourceScreen;
import welbre.ambercraft.client.screen.ModifyFieldsScreen;
import welbre.ambercraft.client.screen.VoltageSourceScreen;
import welbre.ambercraft.debug.network.NetworkViewerScreen;
import welbre.ambercraft.network.AmberCraftScreenOpenerPayload;

import java.util.ArrayList;
import java.util.function.Function;

public final class AmberCraftScreenHelper {
    public enum TYPES {
        HEAT_SOURCE,
        MODIFY_FIELDS,
        NETWORK_DEBUG_TOOL,
        VOLTAGE_SOURCE_SETTINGS
    }

    @OnlyIn(Dist.CLIENT) public static final ArrayList<Function<FriendlyByteBuf, Screen>> FACTORY = new ArrayList<>(TYPES.values().length);

    static {
        FACTORY.add(HeatSourceScreen::new);
        FACTORY.add(ModifyFieldsScreen::new);
        FACTORY.add(NetworkViewerScreen::new);
        FACTORY.add(VoltageSourceScreen::new);
    }

    /**
     * Opens the screen of Type in the client, only uses in the <code color="orange">CLIENT</code> side!.
     */
    @OnlyIn(Dist.CLIENT)
    public static void openInClient(TYPES type, FriendlyByteBuf buf)
    {
        Function<FriendlyByteBuf, Screen> function = AmberCraftScreenHelper.FACTORY.get(type.ordinal());
        Screen apply = function.apply(buf);
        Minecraft.getInstance().setScreen(apply);
    }

    /**
     * Send a package to the client to open a new screen.<br>
     * only uses in the <code color="blue">SERVER</code> side!.
     */
    public static void openInClient(TYPES type, FriendlyByteBuf buf, ServerPlayer player)
    {
        PacketDistributor.sendToPlayer(player, new AmberCraftScreenOpenerPayload(type, buf));
    }

    @OnlyIn(Dist.CLIENT)
    private AmberCraftScreenHelper() {
    }
}
