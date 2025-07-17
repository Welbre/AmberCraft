package welbre.ambercraft.blockentity;


import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import welbre.ambercraft.AmberCraft;

public class CopperHeatConductorConductorBE extends HeatConductorBE {
    public CopperHeatConductorConductorBE(BlockPos pos, BlockState blockState) {
        super(AmberCraft.BlockEntity.COPPER_HEAT_CONDUCTOR_BE.get(), pos, blockState);
    }
}
