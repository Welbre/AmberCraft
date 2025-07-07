package welbre.ambercraft.module;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;
import java.util.function.*;

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
    private Consumer<T> init;
    private Consumer<T> destroyer;
    private BiConsumer<BlockEntity,T> setter;
    private Predicate<BlockEntity> condition;
    private Function<BlockEntity, T> getter;

    public <K extends BlockEntity> ModuleFactory(
            Class<K> BEClass,
            Consumer<T> init,
            Consumer<T> destroyer,
            BiConsumer<K,T> setter,
            Function<K,T> getter
    ){
        this.init = init;
        this.destroyer = destroyer;
        this.setter = (BiConsumer<BlockEntity, T>) setter;
        this.getter = (Function<BlockEntity, T>) getter;
        condition = BEClass::isInstance;
    }

    public abstract ModuleType<T> getType();

    public final void create(LevelAccessor level, BlockPos pos){
        if (level.isClientSide())
            return;
        BlockEntity entity =  level.getBlockEntity(pos);
        if (condition.test(entity))
            setter.accept(entity,get());
    }

    public final void destroy(LevelAccessor level, BlockPos pos)
    {
        if (level.isClientSide())
            return;
        BlockEntity entity =  level.getBlockEntity(pos);
        if (condition.test(entity))
        {
            T module = getter.apply(entity);
            destroyer.accept(module);
        }
    }

    public Optional<T> getModuleOn(LevelAccessor level, BlockPos pos){
        BlockEntity entity = level.getBlockEntity(pos);
        if (condition.test(entity) && !level.isClientSide())
            return Optional.of(getter.apply(entity));
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

    public void setSetter(BiConsumer<BlockEntity, T> setter) {
        this.setter = setter;
    }

    public void setCondition(Predicate<BlockEntity> condition) {
        this.condition = condition;
    }

    public void setGetter(Function<BlockEntity, T> getter) {
        this.getter = getter;
    }
}
