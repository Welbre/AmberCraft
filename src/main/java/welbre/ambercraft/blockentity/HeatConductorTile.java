package welbre.ambercraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class HeatConductorTile extends HeatBlockEntity {
    public HeatConductorTile(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public void transferHeatToNeighbor(Level level, BlockPos pos) {
        super.transferHeatToNeighbor(level, pos);
        //todo slow code above
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).is(Blocks.ICE))
            {
                transferHeatToEnvironment(0,0.02, HeatBlockEntity.DEFAULT_TIME_STEP);
            }
        }
    }
}
