package welbre.ambercraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.Main;
import welbre.ambercraft.blockentity.GoldHeatConductorBlockEntity;
import welbre.ambercraft.blockentity.HeatBlockEntity;
import welbre.ambercraft.module.HeatModuleDefinition;

public class GoldHeatConductorBlock extends HeatConductorBlock{
    public GoldHeatConductorBlock(Properties p) {
        super(p, 0.3f);
        this.heatDef = new HeatModuleDefinition(node -> node.setThermalConductivity(7.98));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GoldHeatConductorBlockEntity(pos, state);
    }
}
