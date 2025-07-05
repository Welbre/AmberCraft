package welbre.ambercraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import welbre.ambercraft.Main;

public class GoldHeatConductorBlockEntity extends HeatBlockEntity {
    public GoldHeatConductorBlockEntity(BlockPos pos, BlockState blockState) {
        super(Main.Tiles.GOLD_HEAT_CONDUCTOR_TILE.get(), pos, blockState, node -> node.setThermalConductivity(7.98));
    }
}
