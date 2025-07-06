package welbre.ambercraft.module;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public interface ModulesHolder {
    /// Returns all modules that this instance holds.
    Module[] getModules();
    /// Returns all modules in certain face.
    Module[] getModule(Direction direction);

    default <T extends Module> @NotNull T[] getModule(Class<T> aclass, Direction direction){
        Module[] modules = direction == null ? getModules() : getModule(direction);
        List<T> moduleList = new ArrayList<>();
        for (Module module : modules) {
            if (aclass.isInstance(module))
                moduleList.add((T) module);
        }
        return moduleList.toArray((T[]) Array.newInstance(aclass,0));
    }

    default void loadData(CompoundTag tag, HolderLookup.Provider registries){
        CompoundTag main = tag.getCompound("modules");
        for (Module module : getModules())
        {
            CompoundTag _tag = main.getCompound(module.getClass().getName());
            module.readData(_tag, registries);
        }
    }

    default void saveData(CompoundTag tag, HolderLookup.Provider registries){
        CompoundTag main = new CompoundTag();
        for (Module module : getModules())
        {
            var _tag = new CompoundTag();
            module.writeData(_tag, registries);

            main.put(module.getClass().getName(), _tag);
        }
        tag.put("modules", main);
    }
}
