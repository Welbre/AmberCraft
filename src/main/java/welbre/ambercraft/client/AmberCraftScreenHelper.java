package welbre.ambercraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import welbre.ambercraft.client.screen.HeatSourceScreen;
import welbre.ambercraft.client.screen.ModifyFieldsScreen;
import welbre.ambercraft.debug.network.NetworkScreen;

import java.util.ArrayList;
import java.util.function.Function;

public final class AmberCraftScreenHelper {
    public enum TYPES {
        HEAT_SOURCE,
        MODIFY_FIELDS,
        NETWORK_DEBUG_TOOL
    }

    @OnlyIn(Dist.CLIENT) public static final ArrayList<Function<FriendlyByteBuf, Screen>> FACTORY = new ArrayList<>(TYPES.values().length);

    static {
        FACTORY.add(HeatSourceScreen::new);
        FACTORY.add(ModifyFieldsScreen::new);
        FACTORY.add(NetworkScreen::new);
    }

    /**
     * Opens the screen of Type in the client, only uses in the <code color="orange">CLIENT</code> side!.
     */
    @OnlyIn(Dist.CLIENT)
    public static void openInClient(TYPES type, FriendlyByteBuf buf, LocalPlayer player)
    {
        Function<FriendlyByteBuf, Screen> function = AmberCraftScreenHelper.FACTORY.get(type.ordinal());
        Screen apply = function.apply(buf);
        Minecraft.getInstance().setScreen(apply);
    }

    @OnlyIn(Dist.CLIENT)
    private AmberCraftScreenHelper() {
    }
}
