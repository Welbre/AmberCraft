package welbre.ambercraft.module;

import java.util.function.Consumer;
import java.util.function.Supplier;

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
