package welbre.ambercraft.network.facedcable;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.FacedCableBE;
import welbre.ambercraft.cables.CableState;
import welbre.ambercraft.cables.FaceState;
import welbre.ambercraft.cables.FacedCableComponent;

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
            FaceState type = cable.getState().getFaceStatus(face);;
            cable.getState().removeCenter(face);

            final FacedCableBE.UpdateShapeResult result = cable.updateState();
            if (result.changed())
                PacketDistributor.sendToPlayersInDimension((ServerLevel) level, new FacedCableStateChangePayload(cable));
            for (var pos : result.diagonal())
                level.neighborChanged(pos, AmberCraft.Blocks.ABSTRACT_FACED_CABLE_BLOCK.get(), null);

            if (cable.getState().isEmpty())
            {
                level.removeBlock(pos, false);
                level.updateNeighborsAt(pos, AmberCraft.Blocks.ABSTRACT_FACED_CABLE_BLOCK.get());
                for (Direction dir : CableState.GET_FACE_DIRECTIONS(face))
                    level.neighborChanged(pos.relative(dir).relative(face), AmberCraft.Blocks.ABSTRACT_FACED_CABLE_BLOCK.get(), null);
            }
            else
                PacketDistributor.sendToPlayersInDimension((ServerLevel) level, new FacedCableStateChangePayload(pos, cable.getState()));

            if (type != null && !context.player().isCreative())
            {
                var stack = new ItemStack(AmberCraft.Items.FACED_CABLE_BLOCK_ITEM.get(), 1);
                stack.set(AmberCraft.Components.CABLE_DATA_COMPONENT.get(),
                        new FacedCableComponent(type.type, type.data.color));

                var item = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                level.addFreshEntity(item);
            }
        }
    }

    public static final Type<FacedCableRemoveFacePayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AmberCraft.MOD_ID, "faced_cable_remove_face"));
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
