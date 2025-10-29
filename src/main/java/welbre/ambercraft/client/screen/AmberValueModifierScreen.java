package welbre.ambercraft.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import welbre.ambercraft.client.screen.widget.ParameterSet;
import welbre.ambercraft.network.AmberValueModifierPayload;
import welbre.ambercraft.network.UpdateAmberSecureKeyPayload;

public class AmberValueModifierScreen extends Screen {

    public ParameterSet set;
    public final double currentValue;
    public final String name;
    public final String suggestion;
    public final BlockPos pos;
    public final AmberValueModifierPayload.Type type;

    public AmberValueModifierScreen(FriendlyByteBuf buf) {
        super(Component.literal("AmberValueModifierScreen"));
        currentValue = buf.readDouble();
        name = buf.readUtf();
        suggestion = buf.readUtf();
        pos = buf.readBlockPos();
        type = AmberValueModifierPayload.Type.values()[buf.readByte()];
    }

    @Override
    protected void init() {
        super.init();
        final int w = (int) Math.floor(this.width * 0.2f);//uses 60% of the total width
        set = new ParameterSet(
                this,
                this.width / 2 - w,
                this.height / 2,
                w,
                20,
                Component.translatable(name),
                Component.translatable(suggestion),
                font,
                ParameterSet.interpolation::linear,
                currentValue,
                currentValue * 1000.0,
                currentValue / 1000.0
        );
    }

    @Override
    public void onClose() {
        super.onClose();
        PacketDistributor.sendToServer(new AmberValueModifierPayload(UpdateAmberSecureKeyPayload.CLIENT_KEY, type, set.value));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_NUMPADENTER || keyCode == InputConstants.KEY_RETURN)
        {
            onClose();
            return true;
        }
        else
            return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public static FriendlyByteBuf GET_BUFFER(BlockPos pos, AmberValueModifierPayload.Type type, double currentValue, String name, String suggestion)
    {
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeDouble(currentValue);
        buf.writeUtf(name);
        buf.writeUtf(suggestion);
        buf.writeBlockPos(pos);
        buf.writeByte(type.ordinal());
        return buf;
    }
}
