package welbre.ambercraft.blocks.electrical;

import kuse.welbre.sim.electrical.abstractt.Element;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.blockentity.electrical.ElectricalBE;
import welbre.ambercraft.blockentity.heat.HeatBE;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.electrical.ElectricalElementModule;
import welbre.ambercraft.module.electrical.ElectricalModule;

import java.util.Stack;

/**
 * Created to deal with inheritance in the singleton register of the minecraft block/blockEntity system.<br>
 * This class is the main class used in all ambercraft electrical blocks.<br>
 *
 * Extend this class and stack new {@link Module.Consumer} in {@link ElectricalBlock#elementConstructor} and {@link ElectricalBlock#elementDestructor}
 * to modify the behavior in the {@link Block#onPlace(BlockState, Level, BlockPos, BlockState, boolean)} and {@link Block#onRemove(BlockState, Level, BlockPos, BlockState, boolean)} respectively.<br><br>
 *
 * So, if you want to create a new block that has a {@link kuse.welbre.sim.electrical.abstractt.Element electricalElement}, this can be very useful,
 * instead of create a new block class and a new BlockEntity class, register both, copy all the code of other elements in your new block class, the same to the BlockEntity ...,
 * you only need to create a new block class that extend this one, and in the constructor change the elementConstructor to set the element that you desired, and for last add you block in the
 * {@link welbre.ambercraft.AmberCraft.Blocks#ELECTRICAL_BE_USERS} to allow you block to create {@link ElectricalBE}.<br>
 * You can modify the {@link #elementDestructor} to change how the block behaves when destroyed,
 * some like check if is energy stored, and create an explosion if is the case.
 * 
 * @see ElectricalElementModule#SET_ELEMENT_IN_THE_WORLD(Element)
 * @see ElectricalModule#ALLOC_MODULE_CONSUMER
 * @see ElectricalModule#PRE_FREE_MODULE_CONSUMER
 */
public class ElectricalBlock extends Block implements EntityBlock {

    public Stack<Module.Consumer<ElectricalBE, ElectricalElementModule>> elementConstructor = new Stack<>();
    public Stack<Module.Consumer<ElectricalBE, ElectricalElementModule>> elementDestructor = new Stack<>();

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new ElectricalBE(pos,state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        return ModulesHolder::TICK_HELPER;
    }

    public ElectricalBlock(Properties p_49795_) {
        super(p_49795_);
        elementConstructor.push(ElectricalModule::ALLOC_MODULE_CONSUMER);
        elementDestructor.push(ElectricalModule::PRE_FREE_MODULE_CONSUMER);
    }


    @Override
    protected void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        Module.executeInLevel(ElectricalBE.class, level, pos, ElectricalBE::getElectricalModule, elementConstructor);
    }

    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
        Module.executeInLevel(ElectricalBE.class, level, pos, ElectricalBE::getElectricalModule, elementDestructor);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        Module.HANDLE_NEIGHBOR_CHANGED(HeatBE.class, HeatBE::getHeatModule, state, level, pos, neighborBlock, orientation, movedByPiston);
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(state, level, pos, neighbor);
        Module.HANDLE_ON_NEIGHBOR_CHANGE(HeatBE.class, HeatBE::getHeatModule, state, level, pos, neighbor);
    }

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        var result = Module.HANDLE_USE_ITEM_ON(HeatBE.class, HeatBE::getHeatModule, stack, state, level, pos, player, hand, hitResult);
        if (result != null && result.consumesAction())
            return result;

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        var result = Module.HANDLE_USE_WITHOUT_ITEM(HeatBE.class, HeatBE::getHeatModule, state, level, pos, player, hitResult);
        if (result != null && result.consumesAction())
            return result;

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public void stepOn(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Entity entity) {
        Module.HANDLE_STEP_ON(HeatBE.class, HeatBE::getHeatModule, level, pos, state, entity);

        super.stepOn(level,pos,state,entity);
    }
}
