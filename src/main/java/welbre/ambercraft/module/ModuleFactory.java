package welbre.ambercraft.module;

import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ModuleFactory<T extends Module> implements Supplier<T> {
    private final Consumer<T> setter;

    public ModuleFactory(Consumer<T> setter) {
        this.setter = setter;
    }

    public abstract DeferredHolder<ModuleType<?>,? extends ModuleType<T>> getHolder();

    @Override
    public T get() {
        T module = getHolder().get().createModule();
        setter.accept(module);
        return module;
    }
}
