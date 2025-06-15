package welbre.ambercraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.Main;
import welbre.ambercraft.blockentity.CopperHeatConductorTile;

public class CopperHeatConductorBlock extends HeatConductorBlock {
    public CopperHeatConductorBlock(Properties p) {
        super(p);
    }

    @Override
    protected BlockEntityType<? extends BlockEntity> getBlockEntityType() {
        return Main.Tiles.COPPER_HEAT_CONDUCTOR_TILE.get();
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CopperHeatConductorTile(pos, state);
    }
}
