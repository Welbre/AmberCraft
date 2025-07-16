package welbre.ambercraft.module;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

/**
 * This class represents a module, an object used to store AmberCraft related data in the minecraft block entity.<br>
 * If you want to use a module in your BE, create a field of type Module or any implemented class,
 * and instantly initiate it.
 * Remember to assign your BE with {@link ModulesHolder} and pass you modules as returns.
 */
public interface Module {
    void writeData(CompoundTag tag, HolderLookup.Provider registries);
    void readData(CompoundTag tag, HolderLookup.Provider registries);

    void tick();
}
