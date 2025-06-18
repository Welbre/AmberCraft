package welbre.ambercraft.module;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface Module {
    void writeData(CompoundTag tag, HolderLookup.Provider registries);
    void readData(CompoundTag tag, HolderLookup.Provider registries);
    void tick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity);
}
