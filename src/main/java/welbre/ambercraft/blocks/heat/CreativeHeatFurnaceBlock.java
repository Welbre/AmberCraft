package welbre.ambercraft.blocks.heat;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.blockentity.heat.CreativeHeatFurnaceBE;
import welbre.ambercraft.blockentity.heat.HeatBE;
import welbre.ambercraft.blocks.FreeRotationBlockHelper;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.heat.HeatModule;
import welbre.ambercraft.module.network.NetworkModule;

import java.util.Stack;

public class CreativeHeatFurnaceBlock extends HeatBlock<CreativeHeatFurnaceBE> implements EntityBlock
{

    public CreativeHeatFurnaceBlock(Properties p_49795_)
    {
        super(p_49795_);
        FreeRotationBlockHelper.REGISTER_DEFAULT_STATE(this, defaultBlockState());
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return FreeRotationBlockHelper.GET_STATE_FOR_PLACEMENT(this, context);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        FreeRotationBlockHelper.CREATE_BLOCK_STATE_DEFINITION(builder);
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
        InteractionResult result = FreeRotationBlockHelper.USE_ITEM_ON(stack, state, level, pos, player, hand, hitResult);
        if (result.consumesAction())
            return result;

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state)
    {
        return new CreativeHeatFurnaceBE(pos, state);
    }
}
