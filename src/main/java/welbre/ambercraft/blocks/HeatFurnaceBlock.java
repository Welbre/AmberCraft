package welbre.ambercraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.Main;
import welbre.ambercraft.blockentity.HeatFurnaceBE;
import welbre.ambercraft.blocks.parent.AmberHorizontalBlock;
import welbre.ambercraft.module.ModuleFactory;
import welbre.ambercraft.module.heat.HeatModule;

import java.util.concurrent.atomic.AtomicReference;

public class HeatFurnaceBlock extends AmberHorizontalBlock implements EntityBlock {
    public ModuleFactory<HeatModule, HeatFurnaceBE> factory = new ModuleFactory<>(
            HeatFurnaceBE.class,
            Main.Modules.HEAT_MODULE_TYPE,
            heatModule -> {heatModule.alloc(); heatModule.getHeatNode().setThermalConductivity(100.0);},
            HeatModule::free,
            HeatFurnaceBE::setHeatModule,
            HeatFurnaceBE::getHeatModule
    ).setConstructor(HeatModule::init);

    public HeatFurnaceBlock(Properties p) {
        super(p);
        registerDefaultState(getStateDefinition().any().setValue(FurnaceBlock.LIT, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        AtomicReference<InteractionResult> result = new AtomicReference<>();
        factory.getModuleOn(level,pos).ifPresent(m -> result.set(factory.getType().useItemOn(m, stack, state, level, pos, player, hand, hitResult)));
        if (result.get() != null && result.get().consumesAction())
            return result.get();

        if (!level.isClientSide){
             if (stack.getItem() == Items.COAL) {
                if (level.getBlockEntity(pos) instanceof HeatFurnaceBE furnace) {
                    furnace.addPower();
                    stack.consume(10, player);
                    return InteractionResult.SUCCESS;
                }
            } else if (stack.getItem() == Items.FLINT_AND_STEEL) {
                if (level.getBlockEntity(pos) instanceof HeatFurnaceBE furnace)
                {
                    furnace.ignite();
                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                    return InteractionResult.SUCCESS;
                }
            }
        }
        if (stack.getItem() == Items.LEVER || stack.getItem() == Items.FLINT_AND_STEEL || stack.getItem() == Items.COAL)
            return InteractionResult.SUCCESS;

        return super.useItemOn(stack,state,level,pos,player,hand, hitResult);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!state.is(oldState.getBlock()))
            factory.create(level,pos);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()))
            factory.destroy(level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (level.getBlockEntity(pos) instanceof HeatFurnaceBE furnace)
            factory.getType().stepOn(furnace.getHeatModule(),level,pos,state,entity);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HeatFurnaceBE(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == Main.BlockEntity.HEAT_FURNACE_BE.get() ? HeatFurnaceBE::tick : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FurnaceBlock.LIT);
    }
}
