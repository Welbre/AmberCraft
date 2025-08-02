package welbre.ambercraft.blocks.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.HeatBE;
import welbre.ambercraft.module.ModuleFactory;
import welbre.ambercraft.module.heat.HeatModule;

import java.util.concurrent.atomic.AtomicReference;

public abstract class HeatBlock extends Block implements EntityBlock {
    protected ModuleFactory<HeatModule, HeatBE> factory = new ModuleFactory<>(
            HeatBE.class,
            AmberCraft.Modules.HEAT_MODULE_TYPE,
            HeatModule::alloc,
            HeatModule::free,
            HeatBE::setHeatModule,
            HeatBE::getHeatModule
    ).setConstructor(HeatModule::init);

    public HeatBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        factory.getModuleOn(level,pos).ifPresent(m -> factory.getType().neighborChanged(m,state,level,pos,neighborBlock, orientation, movedByPiston));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        //when a heatConductor is placed on the side, the block state updates the property and calls this function, so only if the block it-self changes, call the create function.
        if (!state.is(oldState.getBlock()))
            factory.create(level,pos);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        //when a heatConductor is removed on the side, the block state updates the property and calls this function, so only if the block it-self changes, call the create function.
        if (!state.is(newState.getBlock()))
            factory.destroy(level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        var result = factory.getType().useItemOn(factory.getModuleOn(level,pos).orElse(null),stack,state,level,pos,player,hand,hitResult);
        if (result.consumesAction())
            return result;
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        factory.getModuleOn(level,pos).ifPresent(module -> factory.getType().stepOn(module, level, pos, state, entity));
        super.stepOn(level,pos,state,entity);
    }
}
