package welbre.ambercraft.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.client.screen.oscilloscope.OscilloscopeScreen;
import welbre.ambercraft.item.OscilloscopeItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// Used to update the values in the oscilloscope screen.
public record OscilloscopePayload(PayType payType, int scopeID, int traceID, double value) implements CustomPacketPayload
{
    public enum PayType
    {
        ///add a new trace in the target client
        START,
        ///push new data in the target client.
        PUSH,
        ///remove a trace from the target client.
        REMOVE,
        ///close all watchers from in the server.
        STOP
    }
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
    /// Store the data received from the network to plot all trace in all scopes.
    /// Is organized in [scope_ID][Trace_ID].
    @OnlyIn(Dist.CLIENT)
    public static final HashMap<Integer, HashMap<Integer, DataTrace>> DATA = new HashMap<>();

    public void handleOnClient(IPayloadContext context)
    {
        DataTrace trace = GET_TRACE(scopeID, traceID);
        trace.add(value);

        if (Minecraft.getInstance().screen instanceof OscilloscopeScreen screen && screen.oscilloscope_id == scopeID)
            screen.updateData(trace);//finally, update the screen
    }

    public void handlePacket(IPayloadContext context)
    {
        switch (payType)
        {
            case START -> {
                if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.isClientSide)
                {
                    HashMap<Integer,DataTrace> scope = DATA.get(scopeID);
                    if (scope == null)
                        DATA.put(scopeID, new HashMap<>(Map.of(traceID, new DataTrace(traceID))));
                    else
                        if (GET_TRACE(scopeID, traceID) == null)
                            scope.put(traceID, new DataTrace(traceID));
                        else
                            throw new IllegalStateException("Try to create a duplicated trace(id:0x%X) in the oscilloscope(id:0x%X)".formatted(traceID, scopeID));
                }
            }
            case PUSH -> {
                if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.isClientSide)
                {
                    DataTrace trace = GET_TRACE(scopeID, traceID);
                    if (trace == null)
                        throw new IllegalStateException("Try to push data in a null trace(id:0x%X) in the oscilloscope(id:0x%X)".formatted(traceID, scopeID));
                    else
                        trace.add(value);
                    if (Minecraft.getInstance().screen instanceof OscilloscopeScreen screen && screen.oscilloscope_id == scopeID)
                        screen.updateData(trace);//notify the screen
                }
            }
            case REMOVE -> {
                if (Minecraft.getInstance().level != null && !Minecraft.getInstance().level.isClientSide)
                {
                    //run on server only
                    //todo implement the trace remotion in the oscilloscope payload and the screen!
                }
            }
            case STOP -> {
                if (context.flow().isServerbound())
                {
                    //run on server only
                    OscilloscopeItem.WATCHERS.remove(context.player().getUUID());
                }
            }
        }
    }

    public static final CustomPacketPayload.Type<OscilloscopePayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AmberCraft.MOD_ID, "oscilloscope_data_payload"));

    public static final StreamCodec<FriendlyByteBuf, OscilloscopePayload> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(PayType.class), OscilloscopePayload::payType,
            ByteBufCodecs.INT, OscilloscopePayload::scopeID,
            ByteBufCodecs.INT, OscilloscopePayload::traceID,
            ByteBufCodecs.DOUBLE, OscilloscopePayload::value,
            OscilloscopePayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    ///Gets a scope trace using the scope id and the trace id, create the scope or the trace if needed.
    public static DataTrace GET_TRACE(int scopeID, int traceID)
    {
        HashMap<Integer, DataTrace> scope = DATA.get(scopeID);
        DataTrace trace;

        if (scope == null)
        {
           return null;
        } else {
            //search if already has a trace with the traceID
            trace = scope.getOrDefault(traceID, null);

            //in case that don't find it, create one.
            if (trace == null)
            {
                trace = new DataTrace(traceID);
                scope.put(traceID, trace);
            }
        }

        return trace;
    }
}
