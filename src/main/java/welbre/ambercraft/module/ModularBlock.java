package welbre.ambercraft.module;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;

public interface ModularBlock extends BlockEntityTicker<BlockEntity> {
    ModuleDefinition[] getModuleDefinition();

    @Override
    default void tick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        for (ModuleDefinition definition : getModuleDefinition())
            definition.tick(level,pos,state,blockEntity);
    }
}
