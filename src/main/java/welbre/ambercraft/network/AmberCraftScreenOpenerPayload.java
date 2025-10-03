package welbre.ambercraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.client.AmberCraftScreenHelper;

/// Used by the {@link welbre.ambercraft.client.AmberCraftScreenHelper} to send data to the client and open a screen on it.
public record AmberCraftScreenOpenerPayload(int screenType, byte[] data) implements CustomPacketPayload
{
    public AmberCraftScreenOpenerPayload(AmberCraftScreenHelper.TYPES screenType, FriendlyByteBuf data)
    {
        this(screenType.ordinal(), toBytes(data));
    }

    private static byte[] toBytes(FriendlyByteBuf buf)
    {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }

    public void handleOnClient(final IPayloadContext ignoredContext)
    {
        AmberCraftScreenHelper.TYPES screenType = AmberCraftScreenHelper.TYPES.values()[this.screenType()];
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.copiedBuffer(this.data()));
        AmberCraftScreenHelper.openInClient(screenType, buf);
    }

    public static final CustomPacketPayload.Type<AmberCraftScreenOpenerPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AmberCraft.MOD_ID, "ambercraft_screen_opener_payload"));

    public static final StreamCodec<ByteBuf, AmberCraftScreenOpenerPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, AmberCraftScreenOpenerPayload::screenType,
            ByteBufCodecs.BYTE_ARRAY, AmberCraftScreenOpenerPayload::data,
            AmberCraftScreenOpenerPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
