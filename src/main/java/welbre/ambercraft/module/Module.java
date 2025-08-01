package welbre.ambercraft.module;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * This class represents a module, an object used to store AmberCraft related data in the minecraft block entity.<br>
 * If you want to use a module in your BE, create a field of type Module or any implemented class
 * and immediately initiate it.<br><b> Don't forget to initialize the field, a null module can crash your game</b>.<br>
 * Remember to assign your blockEntity with {@link ModulesHolder} and pass you modules as returns.
 * <p>
 *     To create your own modules read the {@link ModuleType ModuleType} documentation.
 * </p>
 */
public interface Module {
    /// Write the module data to the NBT tag.
    void writeData(CompoundTag tag, HolderLookup.Provider registries);
    /// Read the data from the NBT tag.
    void readData(CompoundTag tag, HolderLookup.Provider registries);
    /// Used in Client and Server sides when the entity is ticking.
    void tick(BlockEntity entity);
}
