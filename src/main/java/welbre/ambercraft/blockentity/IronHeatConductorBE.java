package welbre.ambercraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import welbre.ambercraft.Main;

public class IronHeatConductorBE extends HeatBE {
    public IronHeatConductorBE(BlockPos pos, BlockState blockState) {
        super(Main.BlockEntity.IRON_HEAT_CONDUCTOR_BE.get(), pos, blockState);
    }
}
