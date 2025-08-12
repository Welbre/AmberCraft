package welbre.ambercraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import welbre.ambercraft.blocks.FreeRotationBlock;
import welbre.ambercraft.module.Module;

import static welbre.ambercraft.AmberCraft.BlockEntity.CREATIVE_HEAT_FURNACE_BE;

public class CreativeHeatFurnaceBE extends HeatBE {
    public CreativeHeatFurnaceBE(BlockPos pos, BlockState state) {
        super(CREATIVE_HEAT_FURNACE_BE.get(), pos, state);
    }

    @Override
    public Module[] getModule(Direction direction) {
        BlockState state = this.getLevel().getBlockState(this.getBlockPos());
        Direction local = FreeRotationBlock.APPLY_STATE_ROTATION_IN_GLOBAL_DIRECTION(state, direction);

        if (local == Direction.SOUTH)
            return new Module[]{this.getHeatModule()};
        else if (local == Direction.UP)
            return new Module[]{this.getHeatModule()};


        return new Module[0];
    }
}
