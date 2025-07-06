package welbre.ambercraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.blockentity.GoldHeatConductorConductorBE;
import welbre.ambercraft.module.HeatModuleType;

public class GoldHeatConductorBlock extends HeatConductorBlock{
    public GoldHeatConductorBlock(Properties p) {
        super(p, 0.3f);
        this.heatDef = this.heatDef.changeSetter(module -> {
            module.alloc();
            module.getHeatNode().setThermalConductivity(7.98);
        });
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GoldHeatConductorConductorBE(pos, state);
    }
}
