package welbre.ambercraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.debug.NetworkScreen;
import welbre.ambercraft.module.heat.HeatModule;

import java.io.*;
import java.util.Base64;

public record NetworkViewerPayLoad(String data) implements CustomPacketPayload {
    public NetworkViewerPayLoad(HeatModule module)
    {
        this(convert(module));
    }

    public static void handleOnClient(final NetworkViewerPayLoad payLoad, final IPayloadContext context) {
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(Base64.getDecoder().decode(payLoad.data));

            ObjectInputStream inputStream = new ObjectInputStream(stream);
            HeatModule module = (HeatModule) inputStream.readObject();
            inputStream.close();

            Minecraft.getInstance().setScreen(new NetworkScreen(module));

        } catch (ClassNotFoundException | IOException e)
        {
            throw new RuntimeException(e);
        }
    }












    public static final CustomPacketPayload.Type<NetworkViewerPayLoad> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AmberCraft.MOD_ID, "network_viewer_payload"));

    public static final StreamCodec<ByteBuf, NetworkViewerPayLoad> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            NetworkViewerPayLoad::data,
            NetworkViewerPayLoad::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static String convert(HeatModule module)
    {
        try
        {
            ByteArrayOutputStream array = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(array);
            outputStream.writeObject(module);
            outputStream.close();
            String bytes = Base64.getEncoder().encodeToString(array.toByteArray());
            return bytes;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
