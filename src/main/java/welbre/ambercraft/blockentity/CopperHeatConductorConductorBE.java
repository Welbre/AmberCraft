package welbre.ambercraft.blockentity;


import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import welbre.ambercraft.Main;

public class CopperHeatConductorConductorBE extends HeatConductorBE {
    public CopperHeatConductorConductorBE(BlockPos pos, BlockState blockState) {
        super(Main.BlockEntity.COPPER_HEAT_CONDUCTOR_BE.get(), pos, blockState);
    }
}
