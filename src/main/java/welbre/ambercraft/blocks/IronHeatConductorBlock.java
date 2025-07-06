package welbre.ambercraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.blockentity.IronHeatConductorConductorBE;
import welbre.ambercraft.module.heat.HeatModuleFactory;

public class IronHeatConductorBlock extends HeatConductorBlock {
    public IronHeatConductorBlock(Properties p) {
        super(p, 0.45f);
        this.heatDef = new HeatModuleFactory(module -> {
            module.alloc();
            module.getHeatNode().setThermalConductivity(2.2);
        });
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IronHeatConductorConductorBE(pos, state);
    }
}
