package welbre.ambercraft.module;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;
import welbre.ambercraft.module.heat.HeatModuleType;

import java.util.Optional;
import java.util.function.*;

/**
 * This is a fundamental piece in the AmberCraft module system.<br>
 * Is used to help a Block that contains a module in his BlockEntity.
 * To use it, create a new {@link ModuleFactory#ModuleFactory} or use a class that implements this one.<br>
 * The {@link ModuleFactory#create} should be called in the {@link Block#onPlace} after the super method call,<br>
 * The {@link ModuleFactory#destroy} should be called in the {@link Block#onRemove} before the super method call.
 * <p>
 * Notice that both {@link Block#onPlace} and {@link Block#onRemove} is invoked when a property in blockState changes,
 * so, you should check if the block it-self changes on the blockState using {@link BlockState#is(Block)} and only if the block changes you call the correspondent ModuleFactory method.
 * The factory contains some helps to use in your block, the {@link ModuleFactory#getModuleOn(LevelAccessor, BlockPos blockPos)} returns an optional with the module in the blockEntity at blockPos,
 * and the {@link Optional#ifPresent} can be used to call the events in {@link HeatModuleType}.<br>
 *
 *
 * <h2>Internal Operation</h2>
 * <dl>
 *     <dt><h4>Creating</h4></dt>
 *     <dd>
 *         <ul>
 *             <li>The {@link ModuleFactory#create(LevelAccessor, BlockPos pos)} is called to create the module and initiate it.
 *             <ul>
 *                 <li><i>Returns if is called from the client side.</i></li>
 *                 <li><i>Returns if BlockEntity at <code>pos</code> isn't a <code color="orange">instanceof</code> the class passed in the constructor.</i></li>
 *             </ul>
 *             </li>
 *             <li>Invoke {@link ModuleType#createModule()} from the {@link ModuleFactory#holder} to instantiate the module.</li>
 *             <li>{@link ModuleFactory#initializer} accept the module created in the last steep.</li>
 *             <li>{@link ModuleFactory#constructor} is invoked to set up all field and the module parameters.<br><i>Notice that the constructor can be null.</i></li>
 *             <li>{@link ModuleFactory#setter} set the new ready to be used module in the BlockEntity at <code>pos</code>.</li>
 *         </ul>
 *     </dd>

 *     <dt><h4>Destroying</h4></dt>
 *     <dd>
 *          <ul>
 *              <li>The {@link ModuleFactory#destroy(LevelAccessor, BlockPos pos)} is called to destroy and remove the module.
 *              <ul>
 *                      <li><i>Returns if is called from the client side.</i></li>
 *                      <li><i>Returns if BlockEntity at <code>pos</code> isn't a <code color="orange">instanceof</code> the class passed in the constructor.</i></li>
 *              </ul>
 *              </li>
 *              <li>{@link ModuleFactory#getter} is invoked to get the module in the BlockEntity at <code>pos</code>.</li>
 *              <li>{@link ModuleFactory#destroyer} accept the module from the preview step.</li>
 *          </ul>
 *     </dd>
 * </dl>
 * @see ModuleType
 */
public class ModuleFactory<T extends Module, V extends ModulesHolder> implements Supplier<T> {
    private final DeferredHolder<ModuleType<?>,? extends ModuleType<T>> holder;
    private Consumer<T> initializer;
    private ModuleConstructor<T, V, ModuleFactory<T, V>> constructor;
    private Consumer<T> destroyer;
    private BiConsumer<V, T> setter;
    private Function<V, T> getter;
    private Predicate<BlockEntity> condition;

    /**
     * This is the main constructor of the ModuleFactory.<br>
     * Notice that the <code>init</code> doesn't create the module object it-self, only creates all resource needed to work properly.
     * The {@link ModuleType#createModule()} is the responsible for instantiation.
     * @param blockEntityClass a blockEntity class that this factory will interact with.
     * @param holder the deferred holder that contains the module type used in this factory.
     * @param initializer a consumer that will initiate the module.
     * @param destroyer a consumer that will destroy the module.
     * @param setter a function to set the module in the blockEntity.
     * @param getter a function to get the module in the blockEntity.
     */
    public ModuleFactory(
            Class<V> blockEntityClass,
            DeferredHolder<ModuleType<?>,? extends ModuleType<T>> holder,
            Consumer<T> initializer,
            Consumer<T> destroyer,
            BiConsumer<V,T> setter,
            Function<V,T> getter
    ){
        this.holder = holder;
        this.initializer = initializer;
        this.destroyer = destroyer;
        this.setter = setter;
        this.getter = getter;
        this.condition = blockEntityClass::isInstance;
    }

    private ModuleFactory(ModuleFactory<T, V> factory) {
        this.holder = factory.holder;
        this.initializer = factory.initializer;
        this.constructor = factory.constructor;
        this.destroyer = factory.destroyer;
        this.setter = factory.setter;
        this.getter = factory.getter;
        this.condition = factory.condition;
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
        if (condition.test(entity))
            //noinspection unchecked, is checked in condition predicate.
            return Optional.of(getter.apply((V) entity));
        else
            return Optional.empty();
    }

    @Override
    public T get() {
        T module = getType().createModule();
        initializer.accept(module);
        return module;
    }

    public ModuleFactory<T, V> copy() {return new ModuleFactory<>(this);}

    public ModuleFactory<T,V> setInitializer(Consumer<T> initializer) {
        this.initializer = initializer;
        return this;
    }

    public ModuleFactory<T,V> setDestroyer(Consumer<T> destroyer) {
        this.destroyer = destroyer;
        return this;
    }

    public ModuleFactory<T,V> setSetter(BiConsumer<V, T> setter) {
        this.setter = setter;
        return this;
    }

    public ModuleFactory<T,V> setCondition(Predicate<BlockEntity> condition) {
        this.condition = condition;
        return this;
    }

    public ModuleFactory<T,V> setGetter(Function<V, T> getter) {
        this.getter = getter;
        return this;
    }

    public ModuleFactory<T,V> setConstructor(ModuleConstructor<T, V, ModuleFactory<T,V>> constructor) {
        this.constructor = constructor;
        return this;
    }

    @FunctionalInterface
    public interface ModuleConstructor<T extends Module, V extends ModulesHolder, J extends ModuleFactory<T,V>> {
        void build(T module, V entity, J factory, LevelAccessor level, BlockPos pos);
    }
}
