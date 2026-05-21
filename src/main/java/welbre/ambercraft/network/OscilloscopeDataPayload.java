package welbre.ambercraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.client.screen.oscilloscope.OscilloscopeScreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/// Used to update the values in the oscilloscope screen.
public record OscilloscopeDataPayload(int scopeID, int traceID, double value) implements CustomPacketPayload
{
    @OnlyIn(Dist.CLIENT)
    public static class DataTrace
    {
        /// The inicial data alloc a total of 2400 doubles (19kb), enough for 2 minutes with 20 data per second
        public static final int INICIAL_LENGTH = 2 * 60 * 20;

        /// The trace id.
        public final int id;
        /// Used to write data.
        public int head;
        /// Used to read data.
        public int bottom;
        public double[] data;

        public DataTrace(int id) {
            this.id = id;
            head = 0;
            data = new double[INICIAL_LENGTH];
        }

        ///expand the data length to the double
        private void expand()
        {
            //if is more than 30 min open, re-set it
            if (data.length * 2 > 36000)
            {
                data = new double[1000];
                head = 0;
                bottom = 0;
            }
            else
            {
                //create an array with the double and copy the old data to it
                var temp = new double[data.length * 2];
                System.arraycopy(data, 0, temp, 0, data.length);
                data = temp;
            }
        }

        public void add(double value)
        {
            if (head >= data.length)
                expand();

            data[head++] = value;
        }
    }
    @OnlyIn(Dist.CLIENT)
    /// Store the data received from the network to plot all trace in all scopes.
    /// Is organized in [scope_ID].
    public static final HashMap<Integer, List<DataTrace>> DATA = new HashMap<>();

    public void handleOnClient(IPayloadContext context)
    {
        DataTrace trace = GET_TRACE(scopeID, traceID);
        trace.add(value);

        if (Minecraft.getInstance().screen instanceof OscilloscopeScreen screen && screen.oscilloscope_id == scopeID)
            screen.updateData(trace);//finally, update the screen
    }

    public static final CustomPacketPayload.Type<OscilloscopeDataPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AmberCraft.MOD_ID, "oscilloscope_data_payload"));

    public static final StreamCodec<ByteBuf, OscilloscopeDataPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, OscilloscopeDataPayload::scopeID,
            ByteBufCodecs.INT, OscilloscopeDataPayload::traceID,
            ByteBufCodecs.DOUBLE, OscilloscopeDataPayload::value,
            OscilloscopeDataPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    ///Gets a scope trace using the scope id and the trace id, create the scope or the trace if needed.
    public static DataTrace GET_TRACE(int scopeID, int traceID)
    {
        List<DataTrace> scope = DATA.get(scopeID);
        DataTrace trace = null;
        if (scope == null)
        {
            //create the scope and the trace
            scope = new ArrayList<>();
            trace = new DataTrace(traceID);
            scope.add(trace);
            DATA.put(scopeID, scope);
        } else {
            //search if already has a trace with the traceID
            for (DataTrace t : scope)
            {
                if (t.id == traceID)
                {
                    trace = t;
                    break;
                }
            }
            //in case that don't find it, create one.
            if (trace == null)
            {
                trace = new DataTrace(traceID);
                scope.add(trace);
            }
        }

        return trace;
    }
}
