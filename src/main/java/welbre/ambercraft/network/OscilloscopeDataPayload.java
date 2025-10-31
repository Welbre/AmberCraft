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
import welbre.ambercraft.client.screen.oscilloscope.OscilloscopeScreen;

/// Used to update the values in the oscilloscope screen.
public record OscilloscopeDataPayload(double value) implements CustomPacketPayload
{

    public void handleOnClient(IPayloadContext context)
    {
        if (Minecraft.getInstance().screen instanceof OscilloscopeScreen screen)
            screen.updateData(value);
    }

    public static final CustomPacketPayload.Type<OscilloscopeDataPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AmberCraft.MOD_ID, "oscilloscope_data_payload"));

    public static final StreamCodec<ByteBuf, OscilloscopeDataPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, OscilloscopeDataPayload::value,
            OscilloscopeDataPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
