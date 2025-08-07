package welbre.ambercraft.module;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to represent an object that can contains modules.<br>
 * At the moment, only BlockEntity is compatible with this,
 * and maybe this class will be reimplemented as a {@link net.neoforged.neoforge.capabilities.Capabilities}
 * <p>
 *     This interface contains only 3 methods that are very similar to each other.<br>
 *     {@link ModulesHolder#getModules()} is used to read/write data in the holder, and other internal operations like diagnostic tools and debugging.<br>
 *     {@link ModulesHolder#getModule(Direction direction)} is used to handle the connections, this method should return all modules that can be connected at <code>direction</code>.<br>
 *     {@link ModulesHolder#getModule(Object obj)} should be used in yours ModulesHolder implementation instead of {@link ModulesHolder#getModule(Direction)},
 *     the <code>obj</code> can be cast to a direction or any type you want.<br>Exemple: <pre>
Module[] getModule(Object object){
    if (object instanceof Direction dir){
        return getModule(dir);
    }
    else if(...any other type check){

    } else {
        return new Module[0]; //return a empty array, don't return null!
    }
}
 *     </pre>
 * </p>
 */
public abstract class ModulesHolder extends BlockEntity {

    public ModulesHolder(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    /// Returns all modules that this instance holds.
    public abstract @NotNull Module[] getModules();

    /// Returns all modules in <code>direction</code> face.
    @Deprecated
    public abstract @NotNull Module[] getModule(Direction direction);
    /// Similar to {@link ModulesHolder#getModule(Direction) but used a generic object as extra data.}
    public abstract @NotNull Module[] getModule(Object object);

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        CompoundTag main = tag.getCompound("modules");
        for (Module module : getModules())
        {
            CompoundTag _tag = main.getCompound(module.getClass().getName());
            module.readData(_tag, registries);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag main = new CompoundTag();
        for (Module module : getModules())
        {
            var _tag = new CompoundTag();
            module.writeData(_tag, registries);
            main.put(module.getClass().getName(), _tag);
        }
        tag.put("modules", main);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        var tag = super.getUpdateTag(registries);
        CompoundTag main = new CompoundTag();
        for (Module module : getModules())
        {
            var moduleTag = new CompoundTag();
            module.writeUpdateTag(moduleTag, registries);
            main.put(module.getClass().getName(), moduleTag);
        }
        tag.put("modules", main);

        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag compoundTag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(compoundTag, lookupProvider);
        var tag = compoundTag.getCompound("modules");
        for (Module module : getModules())
        {
            var moduleTag = tag.getCompound(module.getClass().getName());
            module.handleUpdateTag(moduleTag, lookupProvider);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        Module module = null;
        try
        {
            Module[] modules = getModules();
            for (int i = 0; i < modules.length; i++)
            {
                module = modules[i];
                module.onLoad(this);
            }
        }catch (Exception e)
        {
            AmberCraft.LOGGER.error("Error loading heat module for block entity at {} with ID {}", getBlockPos(), module.getID(), e);
            level.removeBlock(getBlockPos(), false);
        }
    }

    public void tick(Level level, BlockPos pos, BlockState state)
    {
            for (Module module : this.getModules())
                module.tick(this);
    }

    /// Use on your EntityBlock passing as a method reference;
    public static <T extends BlockEntity> void TICK_HELPER(Level level, BlockPos pos, BlockState state, T blockEntity)
    {
        if (blockEntity instanceof ModulesHolder holder)
            holder.tick(level, pos, state);
        else
            AmberCraft.LOGGER.error("BlockEntity at {} is not a ModulesHolder!", pos);
    }

    public  <T extends Module> @NotNull T[] getModule(Class<T> aclass, Direction direction){
        Module[] modules = direction == null ? getModules() : getModule(direction);
        List<T> moduleList = new ArrayList<>();
        for (Module module : modules) {
            if (aclass.isInstance(module))
                moduleList.add((T) module);
        }
        return moduleList.toArray((T[]) Array.newInstance(aclass,0));
    }
}
