package welbre.ambercraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.blockentity.CreativeHeatConductorBE;
import welbre.ambercraft.sim.heat.HeatNode;

public class CreativeHeatConductorBlock extends HeatConductorBlock{
    public CreativeHeatConductorBlock(Properties p) {
        super(p, 0.25f);
        this.factory.setInit(module -> {
            HeatNode node = module.alloc();
            node.setThermalConductivity(50.0);
        });
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CreativeHeatConductorBE(pos, state);
    }
}
