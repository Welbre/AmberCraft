package welbre.ambercraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import welbre.ambercraft.module.HeatModule;

public class HeatConductorTile extends HeatBlockEntity {

    public HeatConductorTile(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);

        this.heatModule = new HeatModule(this)
        {
            @Override
            public void transferHeatToNeighbor(Level level, BlockPos pos) {
                super.transferHeatToNeighbor(level, pos);
                for (Direction direction : Direction.values()) {
                    if (level.getBlockState(pos.relative(direction)).is(Blocks.ICE))
                    {
                        transferHeatToEnvironment(0,0.02, HeatModule.DEFAULT_TIME_STEP);
                    }
                }
            }
        };
    }

    public static <T extends BlockEntity> void TICK(Level level1, BlockPos pos, BlockState state1, T blockEntity) {
        if (blockEntity instanceof HeatConductorTile tile)
            tile.heatModule.tick(level1, pos, state1, tile);
    }
}
