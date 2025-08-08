package welbre.ambercraft.module;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

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
    /// Write necessary data in a chunk update.
    void writeUpdateTag(CompoundTag tag, HolderLookup.Provider registries);
    /// Handle the data wrote in {@link Module#writeUpdateTag(CompoundTag, HolderLookup.Provider)}.
    void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider);
    /// Called when the block entity receives an update via network.
    void onDataPacket(Connection net, CompoundTag compound, HolderLookup.Provider lookupProvider);
    /// Get the Identifier, should be a number between 0 and 0xffffff
    int getID();
    /// Only in BlockEntity!
    void onLoad(BlockEntity entity);

    /// Used in Client and Server sides when the entity is ticking.
    void tick(BlockEntity entity);

}
