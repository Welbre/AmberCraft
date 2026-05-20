package welbre.ambercraft.blocks.electrical;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.blockentity.electrical.DirectionalElectricalBE;


/**
 * Should be registred in {@link welbre.ambercraft.AmberCraft.Blocks#DIRECTIONAl_ELECTRICAL_BE_USERS}
 * @see welbre.ambercraft.blocks.electrical.ElectricalBlock
 */
public class DirectionalElectricalBlock extends ElectricalBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public DirectionalElectricalBlock(Properties p) {
        super(p);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new DirectionalElectricalBE(pos,state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
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
}
