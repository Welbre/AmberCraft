package welbre.ambercraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import welbre.ambercraft.AmberCraft;

public class GoldHeatConductorBE extends HeatConductorBE {
    public GoldHeatConductorBE(BlockPos pos, BlockState blockState) {
        super(AmberCraft.BlockEntity.GOLD_HEAT_CONDUCTOR_BE.get(), pos, blockState);
    }
}
