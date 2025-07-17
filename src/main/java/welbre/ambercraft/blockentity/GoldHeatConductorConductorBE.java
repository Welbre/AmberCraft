package welbre.ambercraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import welbre.ambercraft.AmberCraft;

public class GoldHeatConductorConductorBE extends HeatConductorBE {
    public GoldHeatConductorConductorBE(BlockPos pos, BlockState blockState) {
        super(AmberCraft.BlockEntity.GOLD_HEAT_CONDUCTOR_BE.get(), pos, blockState);
    }
}
