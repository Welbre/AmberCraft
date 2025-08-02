package welbre.ambercraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.Module;

public class HeatSinkBE extends HeatBE {
    public double clientTemperature = 0;

    public HeatSinkBE(BlockPos pos, BlockState blockState) {
        super(AmberCraft.BlockEntity.HEAT_SINK_BLOCK_BE.get(), pos, blockState);
    }

    @Override
    public Module[] getModule(Direction direction) {
        if (direction == Direction.DOWN)
            return new Module[]{heatModule};
        return new Module[0];
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        var tag = super.getUpdateTag(registries);
        tag.putDouble("t", this.heatModule.getHeatNode().getTemperature());
        return tag;
    }

    @Override
    public void onDataPacket(@NotNull Connection net, @NotNull ClientboundBlockEntityDataPacket pkt, HolderLookup.@NotNull Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
        clientTemperature = pkt.getTag().getDouble("t");
    }

    public static void TICK(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity)
    {
        HeatBE.TICK(level, pos, state, blockEntity);
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
    }
}
