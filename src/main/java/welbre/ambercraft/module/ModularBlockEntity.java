package welbre.ambercraft.module;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public interface ModularBlockEntity {
    Module[] getModules();
    Module[] getModule(Direction direction);

    default <T extends Module> T[] getModule(Class<T> aclass, Direction direction){
        Module[] modules = direction == null ? getModules() : getModule(direction);
        List<T> moduleList = new ArrayList<>();
        for (Module module : modules) {
            if (aclass.isInstance(module))
                moduleList.add((T) module);
        }
        return (T[]) moduleList.toArray((T[]) Array.newInstance(aclass,0));
    }

    default void loadData(CompoundTag tag, HolderLookup.Provider registries){
        for (Module module : getModules())
            module.readData(tag, registries);
    }

    default void saveData(CompoundTag tag, HolderLookup.Provider registries){
        for (Module module : getModules())
            module.writeData(tag, registries);
    }
}
