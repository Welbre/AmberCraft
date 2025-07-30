package welbre.ambercraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.HeatConductorBE;
import welbre.ambercraft.debug.NetworkScreen;
import welbre.ambercraft.debug.NetworkWrapperModule;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.heat.HeatModule;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;

public record NetworkViewerPayLoad(String data) implements CustomPacketPayload {
    public <T extends BlockEntity & ModulesHolder> NetworkViewerPayLoad(T blockEntity)
    {
        this(convert(blockEntity));
    }


    public static void handleOnClient(final NetworkViewerPayLoad payLoad, final IPayloadContext context) {
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(Base64.getDecoder().decode(payLoad.data()));

            ObjectInputStream inputStream = new ObjectInputStream(stream);
            NetworkWrapperModule<?> wrapper = (NetworkWrapperModule<?>) inputStream.readObject();
            inputStream.close();

            //pedaço de merda para fazer um sistema ridículo funcionar
            Object obj = Class.forName("welbre.ambercraft.debug.NetworkScreen").getDeclaredConstructor(NetworkWrapperModule.class).newInstance(wrapper);
            Minecraft.getInstance().getClass().getMethod("setScreen", Screen.class).invoke(Minecraft.getInstance(), obj);


        } catch (ClassNotFoundException | IOException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException | InvocationTargetException e)
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

    private static <T extends BlockEntity & ModulesHolder> String convert(T conductor)
    {
        try
        {
            ByteArrayOutputStream array = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(array);
            outputStream.writeObject(new NetworkWrapperModule(conductor));
            outputStream.close();
            return Base64.getEncoder().encodeToString(array.toByteArray());
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
