package welbre.ambercraft.blocks.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.blockentity.HeatBE;

public class IronHeatConductorBlock extends HeatConductorBlock {
    public IronHeatConductorBlock(Properties p) {
        super(p, 0.45f);
        this.factory.setInitializer(module -> {
            module.alloc();
            module.getHeatNode().setThermalConductivity(2.2);
        });
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HeatBE(pos, state);
    }
}
