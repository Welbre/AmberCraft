package welbre.ambercraft.module;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Optional;
import java.util.function.*;

/**
 * This is father fundamental piece in the AmberCraft module system.<br>
 * Is used to represent father Block that contains father module in his BlockEntity, to use it,
 * create father new ModuleFactory, or use father class that implements this one, and only in the
 * {@link Block#onPlace onPlace()} call the {@link ModuleFactory#get()} method
 * to create the module and pass it to the BE.<br>
 * This class is only father <child>HOLDER</child> to the {@link ModuleType},
 * is essential because in the registration step, the ModuleType isn't available to create father module,
 * therefore, the only way to create father module, is keeping the reference to the type.
 * @param <T> The module that this factory produces.
 */
public class ModuleFactory<T extends Module, V extends BlockEntity & ModulesHolder> implements Supplier<T> {
    private final DeferredHolder<ModuleType<?>,? extends ModuleType<T>> holder;
    private Consumer<T> init;
    private ModuleConstructor<T, V, ModuleFactory<T, V>> constructor;
    private Consumer<T> destroyer;
    private BiConsumer<V, T> setter;
    private Function<V, T> getter;
    private Predicate<BlockEntity> condition;

    public ModuleFactory(
            Class<V> beClass,
            DeferredHolder<ModuleType<?>,? extends ModuleType<T>> holder,
            Consumer<T> init,
            Consumer<T> destroyer,
            BiConsumer<V,T> setter,
            Function<V,T> getter
    ){
        this.holder = holder;
        this.init = init;
        this.destroyer = destroyer;
        this.setter = setter;
        this.getter = getter;
        this.condition = beClass::isInstance;
    }

    public ModuleType<T> getType(){
        return holder.get();
    }

    @SuppressWarnings("unchecked")
    public final void create(LevelAccessor level, BlockPos pos){
        if (level.isClientSide()) return;

        BlockEntity entity =  level.getBlockEntity(pos);
        if (condition.test(entity))
        {
            T module = get();
            setter.accept((V) entity, module);
            if (constructor != null)
                constructor.build(module,(V) entity,this, level, pos);
        }
    }

    public final void destroy(LevelAccessor level, BlockPos pos)
    {
        if (level.isClientSide())
            return;
        BlockEntity entity =  level.getBlockEntity(pos);
        if (condition.test(entity))
        {
            //noinspection unchecked, is checked in condition predicate.
            T module = getter.apply((V) entity);
            destroyer.accept(module);
        }
    }

    public Optional<T> getModuleOn(LevelAccessor level, BlockPos pos){
        BlockEntity entity = level.getBlockEntity(pos);
        if (condition.test(entity) && !level.isClientSide())
            //noinspection unchecked, is checked in condition predicate.
            return Optional.of(getter.apply((V) entity));
        else
            return Optional.empty();
    }

    @Override
    public T get() {
        T module = getType().createModule();
        init.accept(module);
        return module;
    }

    public void setInit(Consumer<T> init) {
        this.init = init;
    }

    public void setDestroyer(Consumer<T> destroyer) {
        this.destroyer = destroyer;
    }

    public void setSetter(BiConsumer<V, T> setter) {
        this.setter = setter;
    }

    public void setCondition(Predicate<BlockEntity> condition) {
        this.condition = condition;
    }

    public void setGetter(Function<V, T> getter) {
        this.getter = getter;
    }

    public ModuleFactory<T,V> setConstructor(ModuleConstructor<T, V, ModuleFactory<T,V>> constructor) {
        this.constructor = constructor;
        return this;
    }

    @FunctionalInterface
    public interface ModuleConstructor<T extends Module, V extends BlockEntity & ModulesHolder, J extends ModuleFactory<T,V>> {
        void build(T module, V entity, J factory, LevelAccessor level, BlockPos pos);
    }
}
