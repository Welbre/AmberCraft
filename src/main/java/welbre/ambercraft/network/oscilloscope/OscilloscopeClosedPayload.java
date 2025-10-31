package welbre.ambercraft.network.oscilloscope;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.client.screen.oscilloscope.OscilloscopeScreen;
import welbre.ambercraft.item.OscilloscopeItem;


/// Used in the {@link welbre.ambercraft.item.OscilloscopeItem} and {@link OscilloscopeScreen} to communicate to the server that a player closes
/// the oscilloscope screen.
public record OscilloscopeClosedPayload() implements CustomPacketPayload
{

    public void handleOnServer(IPayloadContext context)
    {
        OscilloscopeItem.WATCHERS.remove(context.player().getUUID());
    }

    public static final CustomPacketPayload.Type<OscilloscopeClosedPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AmberCraft.MOD_ID, "oscilloscope_closed_payload"));

    public static final StreamCodec<ByteBuf, OscilloscopeClosedPayload> STREAM_CODEC = StreamCodec.unit(new OscilloscopeClosedPayload());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
