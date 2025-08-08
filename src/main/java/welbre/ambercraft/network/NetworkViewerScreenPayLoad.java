package welbre.ambercraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.client.AmberCraftScreenHelper;
import welbre.ambercraft.debug.network.NetworkDebugHelper;
import welbre.ambercraft.module.ModulesHolder;

import java.io.*;
import java.util.Base64;

public record NetworkViewerScreenPayLoad(String data) implements CustomPacketPayload {

    public <T extends ModulesHolder> NetworkViewerScreenPayLoad(T blockEntity)
    {
        this(convert(blockEntity));
    }


    public static void handleOnClient(final NetworkViewerScreenPayLoad payLoad, final IPayloadContext context) {
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(payLoad.data());
        AmberCraftScreenHelper.openInClient(AmberCraftScreenHelper.TYPES.NETWORK_DEBUG_TOOL, buf, (LocalPlayer) context.player());
    }


    private static <T extends ModulesHolder> String convert(T conductor)
    {
        try
        {
            ByteArrayOutputStream array = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(array);
            outputStream.writeObject(new NetworkDebugHelper(conductor));
            outputStream.close();
            return Base64.getEncoder().encodeToString(array.toByteArray());
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static final CustomPacketPayload.Type<NetworkViewerScreenPayLoad> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AmberCraft.MOD_ID, "network_viewer_payload"));

    public static final StreamCodec<ByteBuf, NetworkViewerScreenPayLoad> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, NetworkViewerScreenPayLoad::data,
            NetworkViewerScreenPayLoad::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
