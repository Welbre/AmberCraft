package welbre.ambercraft.blockentity;


import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import welbre.ambercraft.Main;
import welbre.ambercraft.module.HeatModule;
import welbre.ambercraft.sim.heat.HeatNode;

import java.util.function.Consumer;

public class CopperHeatConductorTile extends HeatBlockEntity {
    public CopperHeatConductorTile(BlockPos pos, BlockState blockState) {
        super(Main.Tiles.COPPER_HEAT_CONDUCTOR_TILE.get(), pos, blockState, node -> node.setThermalConductivity(10.0));
    }
}
