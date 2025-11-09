package welbre.ambercraft.blocks.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.blockentity.heat.HeatBE;
import welbre.ambercraft.sim.heat.HeatNode;

public class CreativeHeatConductorBlock extends HeatConductorBlock{
    public CreativeHeatConductorBlock(Properties p) {
        super(p, 0.25f);
        moduleConstructor.push((module, holder, level, pos) -> module.getHeatNode().setThermalConductivity(50.0));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new HeatBE(pos, state);
    }
}
