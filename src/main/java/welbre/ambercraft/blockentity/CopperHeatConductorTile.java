package welbre.ambercraft.blockentity;


import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import welbre.ambercraft.Main;

public class CopperHeatConductorTile extends HeatConductorTile {
    public CopperHeatConductorTile(BlockPos pos, BlockState blockState) {
        super(Main.Tiles.COPPER_HEAT_CONDUCTOR_TILE.get(), pos, blockState);
        this.heatModule.setThermalConductivity(10.0);
    }
}
