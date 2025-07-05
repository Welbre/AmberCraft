package welbre.ambercraft.module;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public interface Module {
    void writeData(CompoundTag tag, HolderLookup.Provider registries);
    void readData(CompoundTag tag, HolderLookup.Provider registries);
}
