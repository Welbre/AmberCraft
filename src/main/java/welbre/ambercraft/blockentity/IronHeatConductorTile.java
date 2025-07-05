package welbre.ambercraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import welbre.ambercraft.Main;

public class IronHeatConductorTile extends HeatBlockEntity {
    public IronHeatConductorTile(BlockPos pos, BlockState blockState) {
        super(Main.Tiles.IRON_HEAT_CONDUCTOR_TILE.get(), pos, blockState);
    }
}
