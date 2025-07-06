package welbre.ambercraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.blockentity.CopperHeatConductorConductorBE;
import welbre.ambercraft.module.HeatModuleType;

public class CopperHeatConductorBlock extends HeatConductorBlock {
    public CopperHeatConductorBlock(Properties p) {
        super(p, 0.4f);
        this.heatDef = this.heatDef.changeSetter(module -> {
            module.alloc();
            module.getHeatNode().setThermalConductivity(10.0);
        });
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CopperHeatConductorConductorBE(pos, state);
    }
}
