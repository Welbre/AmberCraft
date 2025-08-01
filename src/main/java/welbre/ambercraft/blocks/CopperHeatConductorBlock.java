package welbre.ambercraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.blockentity.HeatConductorBE;
import welbre.ambercraft.sim.heat.HeatNode;

public class CopperHeatConductorBlock extends HeatConductorBlock {
    public CopperHeatConductorBlock(Properties p) {
        super(p, 0.4f);
        this.factory.setInitializer(module -> {
            HeatNode node = module.alloc();
            node.setThermalConductivity(10.0);
        });
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HeatConductorBE(pos, state);
    }
}
