package welbre.ambercraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import welbre.ambercraft.AmberCraft;

public class CreativeHeatConductorBE extends HeatConductorBE{
    public CreativeHeatConductorBE(BlockPos pos, BlockState blockState) {
        super(AmberCraft.BlockEntity.CREATIVE_HEAT_CONDUCTOR_BE.get(), pos, blockState);
    }
}
