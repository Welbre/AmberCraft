package welbre.ambercraft.blocks.electrical;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.blockentity.electrical.GroundBE;
import welbre.ambercraft.module.ModulesHolder;

public class GroundBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public GroundBlock(Properties p) {
        super(p);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown())
            return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
        else
            return this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new GroundBE(pos,state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        return ModulesHolder::TICK_HELPER;
    }

    @Override
    protected void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.getBlockEntity(pos) instanceof GroundBE groundBE)
        {
            groundBE.groundModule = new GroundBE.GroundModule(0.005);
            groundBE.groundModule.alloc();
            groundBE.groundModule.refresh(groundBE);
        }
    }

    @Override
    protected void onRemove(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof GroundBE groundBE)
        {
            groundBE.groundModule.free();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }








    @Override
    public void stepOn(Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Entity entity) {
        if (level.getBlockEntity(pos) instanceof GroundBE groundBE)
            groundBE.groundModule.getType().stepOn(groundBE.groundModule,level,pos,state,entity);
        super.stepOn(level, pos, state, entity);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof GroundBE gnd)
        {
            InteractionResult result = gnd.groundModule.getType().useWithoutItem(gnd.groundModule, state, level, pos, player, hitResult);
            if (result.consumesAction())
                return result;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected @NotNull InteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof GroundBE gnd)
        {
            InteractionResult result = gnd.groundModule.getType().useItemOn(gnd.groundModule, stack, state, level, pos, player, hand, hitResult);
            if (result.consumesAction())
                return result;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected void neighborChanged(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof GroundBE gnd)
            gnd.groundModule.getType().neighborChanged(gnd.groundModule,state,level,pos,neighborBlock, orientation, movedByPiston);
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
    }

    @Override
    public void onNeighborChange(@NotNull BlockState state, LevelReader level, @NotNull BlockPos pos, @NotNull BlockPos neighbor) {
        if (level.getBlockEntity(pos) instanceof GroundBE gnd)
            gnd.groundModule.getType().onNeighborChange(gnd.groundModule,state,level,pos,neighbor);
        super.onNeighborChange(state, level, pos, neighbor);
    }
}
