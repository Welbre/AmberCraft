package welbre.ambercraft.module;

import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This is a fundamental piece in AmberCraft module system.<br>
 * Is used to represent a Block that contains a module in his BlockEntity, to use it,
 * create a new ModuleFactory, or use a class that implements this one, and only in the
 * {@link Block#onPlace onPlace()} call the {@link ModuleFactory#get()} method
 * to create the module and pass it to the BE.<br>
 * This class is only a <b>HOLDER</b> to the {@link ModuleType},
 * is essential because in the registration step, the ModuleType isn't available to create a module,
 * therefore the only way to create a module, is keeping the reference to the type.
 * @param <T>
 */
public abstract class ModuleFactory<T extends Module> implements Supplier<T> {
    private final Consumer<T> setter;

    public ModuleFactory(Consumer<T> setter) {
        this.setter = setter;
    }

    public abstract ModuleType<T> getType();

    @Override
    public T get() {
        T module = getType().createModule();
        setter.accept(module);
        return module;
    }
}
