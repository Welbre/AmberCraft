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
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.network.NetworkModule;

import java.io.*;
import java.util.Arrays;
import java.util.Base64;

public record NetworkViewerScreenPayLoad(String data, BlockPos pos) implements CustomPacketPayload {

    public NetworkViewerScreenPayLoad(ModulesHolder holder)
    {
        this(convert(holder), holder.getBlockPos());
    }


    public static void handleOnClient(final NetworkViewerScreenPayLoad payLoad, final IPayloadContext context) {
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBlockPos(payLoad.pos());
        buf.writeUtf(payLoad.data());
        // The array wrote in the convert step, should be unserialized in the Screen constructor using the buffer.
        AmberCraftScreenHelper.openInClient(AmberCraftScreenHelper.TYPES.NETWORK_DEBUG_TOOL, buf, (LocalPlayer) context.player());
    }


    public static NetworkModule[] ModulesFromString(String data)
    {
        try
        {
            ByteArrayInputStream array = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            ObjectInputStream inputStream = new ObjectInputStream(array);

            return (NetworkModule[]) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static String convert(ModulesHolder holder)
    {
        try
        {
            ByteArrayOutputStream array = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(array);
            // write a networkModules array in the stream
            outputStream.writeObject(Arrays.stream(holder.getModules()).filter(a -> a instanceof NetworkModule).toArray(NetworkModule[]::new));

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
            BlockPos.STREAM_CODEC, NetworkViewerScreenPayLoad::pos,
            NetworkViewerScreenPayLoad::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
