package welbre.ambercraft.module;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;

/**
 * This class represents a module, an object used to store AmberCraft related data in the minecraft block entity.<br>
 * If you want to use a module in your BE, create a field of type Module or any implemented class
 * and immediately initiate it.<br><b> Don't forget to initialize the field, a null module can crash your game</b>.<br>
 * Remember to assign your blockEntity with {@link ModulesHolder} and pass you modules as returns.
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
    void onLoad(ModulesHolder entity);

    default InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult){return InteractionResult.PASS;}
    default InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult){return InteractionResult.PASS;}
    default void stepOn(Level level, BlockPos pos, BlockState state, Entity entity){}
    default void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston){}
    default void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor){}

    /// Used in Client and Server sides when the entity is ticking.
    void tick(ModulesHolder entity);

    /**
     * A function that is used in the {@link #executeInLevel(Class, Level, BlockPos, Function, Consumer)}
     * @param <K>
     * @param <T>
     */
    @FunctionalInterface
    interface Consumer<K extends ModulesHolder, T extends Module>
    {
        void run(T module, K holder, Level level, BlockPos pos);
    }

    /**
     * A helper to deal with the boilerplate of get and run some operation in a module.<br>
     * Do all the hard work of get a block entity, check the type, get the module,
     * check if the module isn't null, any only after all, the run the consumer.<br>
     *
     * @param holderClass the ModulesHolder class expected at this position
     * @param level the minecraft level
     * @param pos the position of the ModulesHolder in the world
     * @param getter a function to get the module desired in the ModulesHolder
     * @param consumer the consumer that will execute some operation using the module and the ModulesHolder
     * @param <K> Generic type specified my the ModulesHolderClass
     *
     * @see Module#getInLevel(Class, Level, BlockPos, Function)
     */
    static <K extends ModulesHolder, T extends Module> void executeInLevel(
            @NotNull Class<K> holderClass,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull Function<K,@Nullable T> getter,
            @NotNull Module.Consumer<K, T> consumer
    )
    {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (holderClass.isInstance(blockEntity))
        {
            K entity = holderClass.cast(blockEntity);

            T module = getter.apply(entity);
            if (module != null)
                consumer.run(module, entity, level, pos);
        }
    }

    /**
     * Similar of {@link #executeInLevel(Class, Level, BlockPos, Function, Consumer)} but run each consumer in a collection.
     *
     * @param holderClass the ModulesHolder class expected at this position
     * @param level the minecraft level
     * @param pos the position of the ModulesHolder in the world
     * @param getter a function to get the module desired in the ModulesHolder
     * @param consumers a collection of consumers that will be called in the {@link Collection#iterator()} order
     * @param <K> Generic type specified my the ModulesHolderClass
     *
     * @see Module#getInLevel(Class, Level, BlockPos, Function)
     */
    static <K extends ModulesHolder, T extends Module> void executeInLevel(
            @NotNull Class<K> holderClass,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull Function<K,@Nullable T> getter,
            @NotNull Collection<Consumer<K, T>> consumers
    )
    {
        executeInLevel(holderClass, level, pos, getter, (Consumer<K, T>) (module, entity, level1, pos1) -> {
            for (Consumer<K, T> consumer : consumers)
                consumer.run(module, entity, level1, pos1);
        });
    }


    /**
     * A helper to deal with the boilerplate of get a module in the world.<br>
     * This function get the {@link ModulesHolder} in the world, check if it is a <code>holderClass</code> and only them run the get method.
     *
     * @param holderClass the ModulesHolder class expected at this position
     * @param level the minecraft level
     * @param pos the position of the ModulesHolder in the world
     * @param getter a function to get the module desired in the ModulesHolder
     * @param <K> Generic type specified my the ModulesHolderClass
     * @param <T> The module type that will be obtained in the getter and returned by the method
     */
    static <K extends ModulesHolder, T extends Module> @Nullable T getInLevel(
            @NotNull Class<K> holderClass,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull Function<K, T> getter
    )
    {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (holderClass.isInstance(blockEntity))
        {
            K entity = holderClass.cast(blockEntity);
            return getter.apply(entity);
        }
        return null;
    }

    /**
     * A helper to call handles use item on in block class
     */
    static <T extends ModulesHolder> @Nullable InteractionResult HANDLE_USE_ITEM_ON(Class<T> holder, @NotNull Function<T, ? extends Module> getter, ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        InteractionResult[] result = new InteractionResult[1];
        executeInLevel(holder, level, pos, getter, (module, modulesHolder, level1, pos1) ->
                result[0] = module.useItemOn(stack, state, level1, pos1, player, hand, hitResult));

        return result[0];
    }

    /// A helper to call handles use_without_item in then block class
    static <T extends ModulesHolder> @Nullable InteractionResult HANDLE_USE_WITHOUT_ITEM(Class<T> holder, @NotNull Function<T, ? extends Module> getter, BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult)
    {
        InteractionResult[] result = new InteractionResult[1];
        executeInLevel(holder, level, pos, getter, (module, modulesHolder, level1, pos1) ->
                result[0] = module.useWithoutItem(state, level1, pos1, player, hitResult));

        return result[0];
    }

    /// A helper to call handles step_on in then block class
    static <T extends ModulesHolder> void HANDLE_STEP_ON(Class<T> holder, @NotNull Function<T, ? extends Module> getter, Level level, BlockPos pos, BlockState state, Entity entity)
    {
        executeInLevel(holder, level, pos, getter, (module, modulesHolder, level1, pos1) -> module.stepOn(level1, pos1, state, entity));
    }

    /// A helper to call handles neighbor_changed in then block class
    static <T extends ModulesHolder> void HANDLE_NEIGHBOR_CHANGED(Class<T> holder, @NotNull Function<T, ? extends Module> getter, BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston)
    {
        executeInLevel(holder, level, pos, getter, (module, modulesHolder, level1, pos1) -> module.neighborChanged(state, level1, pos1, neighborBlock, orientation, movedByPiston));
    }

    /// A helper to call handles on_neighbor_change in then block class
    static <T extends ModulesHolder> void HANDLE_ON_NEIGHBOR_CHANGE(Class<T> holder, @NotNull Function<T, ? extends Module> getter, BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor)
    {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (holder.isInstance(blockEntity))
        {
            T entity = holder.cast(blockEntity);
            Module module = getter.apply(entity);
            if (module != null)
                module.onNeighborChange(state, level, pos, neighbor);
        }
    }

}
