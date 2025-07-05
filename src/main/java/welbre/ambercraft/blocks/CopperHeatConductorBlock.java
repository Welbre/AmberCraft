package welbre.ambercraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.Main;
import welbre.ambercraft.blockentity.CopperHeatConductorTile;
import welbre.ambercraft.blockentity.HeatBlockEntity;
import welbre.ambercraft.module.HeatModuleDefinition;

public class CopperHeatConductorBlock extends HeatConductorBlock {
    public CopperHeatConductorBlock(Properties p) {
        super(p, 0.4f);
        this.heatDef = new HeatModuleDefinition(node -> node.setThermalConductivity(10.0));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CopperHeatConductorTile(pos, state);
    }
}
