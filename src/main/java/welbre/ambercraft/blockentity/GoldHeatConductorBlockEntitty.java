package welbre.ambercraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import welbre.ambercraft.Main;

public class GoldHeatConductorBlockEntitty extends HeatBlockEntity {
    public GoldHeatConductorBlockEntitty(BlockPos pos, BlockState blockState) {
        super(Main.Tiles.GOLD_HEAT_CONDUCTOR_TILE.get(), pos, blockState);
        this.heatModule.getHeatNode().setThermalConductivity(7.95);
    }
}
