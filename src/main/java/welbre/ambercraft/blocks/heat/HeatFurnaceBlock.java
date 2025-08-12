package welbre.ambercraft.blocks.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.HeatFurnaceBE;
import welbre.ambercraft.module.ModuleFactory;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.heat.HeatModule;


import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class HeatFurnaceBlock extends HeatBlock {
    public static final EnumProperty<Direction> FACING = HORIZONTAL_FACING;

    public ModuleFactory<HeatModule, HeatFurnaceBE> factory = new ModuleFactory<>(
            HeatFurnaceBE.class,
            AmberCraft.Modules.HEAT_MODULE_TYPE,
            HeatModule::alloc,
            HeatModule::free,
            HeatFurnaceBE::setHeatModule,
            HeatFurnaceBE::getHeatModule
    ).setConstructor((module, entity, factory, level, pos) -> {
        module.init(entity, factory, level, pos);
        module.getHeatNode().setThermalConductivity(100.0);
    });

    public HeatFurnaceBlock(Properties p) {
        super(p);
        registerDefaultState(getStateDefinition().any().setValue(FurnaceBlock.LIT, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        var result = factory.getType().useItemOn(factory.getModuleOn(level,pos).orElse(null),stack,state,level,pos,player,hand,hitResult);
        if (result.consumesAction())
            return result;

        if (level.getBlockEntity(pos) instanceof HeatFurnaceBE furnace)
        {
            if (stack.getItem() == Items.COAL)
            {
                if (!level.isClientSide)
                {
                    furnace.addPower();
                    stack.consume(10, player);
                }
                return InteractionResult.SUCCESS;
            } else if (stack.getItem() == Items.FLINT_AND_STEEL)
            {
                if (!level.isClientSide)
                {
                    furnace.ignite();
                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                }
                return InteractionResult.SUCCESS;
            }
        }

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
        return ModulesHolder::TICK_HELPER;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FurnaceBlock.LIT);
        builder.add(HeatFurnaceBlock.FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(FurnaceBlock.LIT))
        {
            if (level.getBlockEntity(pos) instanceof HeatFurnaceBE furnace)
                if (random.nextDouble() < 0.1 * furnace.getPower() / 10.0)
                    level.playLocalSound(pos, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, (float) (1.0f * furnace.getPower() / 20f), 1.5f, false);
        }
    }
}
