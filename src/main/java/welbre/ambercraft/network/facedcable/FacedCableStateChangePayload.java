package welbre.ambercraft.network.facedcable;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.FacedCableBE;
import welbre.ambercraft.cables.CableState;


public record FacedCableStateChangePayload(BlockPos pos, CableState state) implements CustomPacketPayload {

    public FacedCableStateChangePayload(FacedCableBE cable)
    {
        this(cable.getBlockPos(), cable.getState());
    }

    public void handleOnClient(IPayloadContext context)
    {
        Level level = context.player().level();
        if (level.getBlockEntity(pos) instanceof FacedCableBE cable)
        {
            cable.setState(state);
            Minecraft.getInstance().levelRenderer.setBlocksDirty(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
            cable.requestModelDataUpdate();
        }
    }
    
    
    public static final StreamCodec<ByteBuf, FacedCableStateChangePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, FacedCableStateChangePayload::pos,
            CableState.STREAM_CODEC, FacedCableStateChangePayload::state,
            FacedCableStateChangePayload::new
    );
    
    public static final Type<FacedCableStateChangePayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AmberCraft.MOD_ID, "faced_cable_state_change"));
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
