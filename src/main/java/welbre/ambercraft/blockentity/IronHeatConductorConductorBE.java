package welbre.ambercraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import welbre.ambercraft.AmberCraft;

public class IronHeatConductorConductorBE extends HeatConductorBE {
    public IronHeatConductorConductorBE(BlockPos pos, BlockState blockState) {
        super(AmberCraft.BlockEntity.IRON_HEAT_CONDUCTOR_BE.get(), pos, blockState);
    }
}
