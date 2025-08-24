package welbre.ambercraft.network.facedcable;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.FacedCableBE;
import welbre.ambercraft.cables.FaceState;

public record FacedCableRemoveFacePayload(BlockPos pos, Direction face) implements CustomPacketPayload {

    public static final StreamCodec<ByteBuf, FacedCableRemoveFacePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, FacedCableRemoveFacePayload::pos,
            Direction.STREAM_CODEC, FacedCableRemoveFacePayload::face,
            FacedCableRemoveFacePayload::new
    );

    public void handleOnServer(IPayloadContext context)
    {
        Level level = context.player().level();
        if (level.getBlockEntity(pos) instanceof FacedCableBE cable)
        {
            if (!context.player().isCreative())//drop center
                cable.dropCenter(face);

            cable.getState().removeCenter(face);//remove center

            //update diagonal and neighbors
            cable.updateFaceNeighborhood(face);
            level.updateNeighborsAt(pos, AmberCraft.Blocks.ABSTRACT_FACED_CABLE_BLOCK.get());

            // remove if is empty, else update
            if (cable.getState().isEmpty())
                level.removeBlock(pos, false);
            else
            {
                cable.updateState();
                cable.setChanged();
                PacketDistributor.sendToPlayersInDimension((ServerLevel) level, new FacedCableStateChangePayload(cable));
            }
        }
    }

    public static final Type<FacedCableRemoveFacePayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AmberCraft.MOD_ID, "faced_cable_remove_face"));
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
