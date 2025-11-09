package welbre.ambercraft.blocks.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.blockentity.heat.HeatBE;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.heat.HeatModule;
import welbre.ambercraft.module.network.NetworkModule;

import java.util.Stack;

/**
 * Created to deal with inheritance in the singleton register of the minecraft block/blockEntity system.<br>
 * This class is the main class used in all ambercraft thermal blocks.<br>
 *
 * <i><b>This is similar to {@link welbre.ambercraft.blocks.electrical.ElectricalBlock ElectricalBlock} but to thermal elements.</b></i>
 */
public abstract class HeatBlock<T extends HeatBE> extends Block implements EntityBlock {
    public final Class<T> beClass;
    public Stack<Module.Consumer<T, HeatModule>> moduleConstructor = new Stack<>();
    public Stack<Module.Consumer<T, HeatModule>> moduleDestructor = new Stack<>();

    public HeatBlock(Properties p)
    {
        this(p, (Class<T>) HeatBE.class);
    }

    public HeatBlock(Properties p_49795_, Class<T> beClass) {
        super(p_49795_);
        this.beClass = beClass;
        moduleConstructor.push(NetworkModule::ALLOC_MODULE_CONSUMER);
        moduleConstructor.push(HeatModule::init);
        moduleDestructor.push(NetworkModule::FREE_MODULE_CONSUMER);
    }

    @Override
    protected void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        Module.HANDLE_NEIGHBOR_CHANGED(beClass, HeatBE::getHeatModule, state, level, pos, neighborBlock, orientation, movedByPiston);
    }

    @Override
    public void onNeighborChange(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos, @NotNull BlockPos neighbor) {
        super.onNeighborChange(state, level, pos, neighbor);
        Module.HANDLE_ON_NEIGHBOR_CHANGE(beClass, HeatBE::getHeatModule, state, level, pos, neighbor);
    }

    @Override
    protected void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        //when a heatConductor is placed on the side, the block state updates the property and calls this function, so only if the block it-self changes, call the create function.
        if (!state.is(oldState.getBlock()))
            Module.executeInLevel(beClass, level, pos, HeatBE::getHeatModule, moduleConstructor);
    }

    @Override
    protected void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean movedByPiston) {
        //when a heatConductor is removed on the side, the block state updates the property and calls this function, so only if the block it-self changes, call the create function.
        if (!state.is(newState.getBlock()))
            Module.executeInLevel(beClass, level, pos, HeatBE::getHeatModule, moduleDestructor);

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected @NotNull InteractionResult useItemOn(
            @NotNull ItemStack stack,
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull Player player,
            @NotNull InteractionHand hand,
            @NotNull BlockHitResult hitResult)
    {
        var result = Module.HANDLE_USE_ITEM_ON(beClass, HeatBE::getHeatModule, stack, state, level, pos, player, hand, hitResult);
        if (result != null && result.consumesAction())
            return result;

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        var result = Module.HANDLE_USE_WITHOUT_ITEM(beClass, HeatBE::getHeatModule, state, level, pos, player, hitResult);
        if (result != null && result.consumesAction())
            return result;

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public void stepOn(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Entity entity) {
        Module.HANDLE_STEP_ON(beClass, HeatBE::getHeatModule, level, pos, state, entity);

        super.stepOn(level,pos,state,entity);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new HeatBE(pos, state);
    }
}
