package welbre.ambercraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.client.AmberCraftScreenHelper;
import welbre.ambercraft.debug.network.Serialization;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.network.NetworkModule;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

public record NetworkViewerScreenPayLoad(byte[] data, BlockPos pos) implements CustomPacketPayload {

    public NetworkViewerScreenPayLoad(ModulesHolder holder)
    {
        this(convert(holder), holder.getBlockPos());
    }


    public static void handleOnClient(final NetworkViewerScreenPayLoad payLoad, final IPayloadContext context) {
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBlockPos(payLoad.pos());
        buf.writeByteArray(payLoad.data());
        // The array wrote in the convert step, should be unserialized in the Screen constructor using the buffer.
        AmberCraftScreenHelper.openInClient(AmberCraftScreenHelper.TYPES.NETWORK_DEBUG_TOOL, buf, (LocalPlayer) context.player());
    }


    public static NetworkModule[] ModulesFromString(byte[] data)
    {
        try
        {
            Object[] array = Serialization.deserialize(data);
            var x = Arrays.stream(array).filter(NetworkModule.class::isInstance).toArray(NetworkModule[]::new);
            if (x.length == 0)
                throw new IllegalStateException("Invalid unSerialization to create a network viewer screen!");
            return x;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private static byte[] convert(ModulesHolder holder)
    {
        try
        {
            Object[] array = Arrays.stream(holder.getModules()).filter(module -> module instanceof NetworkModule).toArray();
            return Serialization.serialize(array);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static final CustomPacketPayload.Type<NetworkViewerScreenPayLoad> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AmberCraft.MOD_ID, "network_viewer_payload"));

    public static final StreamCodec<ByteBuf, NetworkViewerScreenPayLoad> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE_ARRAY, NetworkViewerScreenPayLoad::data,
            BlockPos.STREAM_CODEC, NetworkViewerScreenPayLoad::pos,
            NetworkViewerScreenPayLoad::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
