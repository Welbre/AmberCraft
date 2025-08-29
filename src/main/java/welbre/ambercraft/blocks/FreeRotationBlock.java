package welbre.ambercraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.datagen.template.AmberModelTemplate;

/**
 * This block is used for each AmberCraft block that can face any of 6 direction and rotate around the facing direction.<br>
 * So they are free to rotate around any axes. Uses a total of 24 block states, 6 (facing) * 4 (rotation around face) = 12 states.<br>
 * This class has helpers to make the used off free block less painful :(.
 */
public class FreeRotationBlock extends Block {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final EnumProperty<Rotation> ROTATION = AmberModelTemplate.ROTATION;

    public FreeRotationBlock(Properties p_49795_) {
        super(p_49795_);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(ROTATION, Rotation.NONE));
    }


    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (hitResult.getDirection() == state.getValue(FACING) && player.getMainHandItem().is(Items.AIR))
        {
            var newState = state.setValue(ROTATION, state.getValue(ROTATION).getRotated(Rotation.CLOCKWISE_90));
            level.setBlock(pos, newState, 3);
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
        builder.add(ROTATION);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        var face = context.getNearestLookingDirection();
        Rotation rotation = Rotation.NONE;
        if (face.getAxis() == Direction.Axis.Y)
            rotation = switch (context.getHorizontalDirection()){
                case SOUTH -> Rotation.NONE;
                case WEST -> face == Direction.DOWN ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90;
                case NORTH -> Rotation.CLOCKWISE_180;
                case EAST -> face == Direction.DOWN ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90;
                default -> null;
            };
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite()).setValue(ROTATION, rotation);
    }

    /**
     * This computes the <code>local</code> face direction of a <code>global</code> direction using the state.<br>
     * In a block that can't be rotated, a global {@link Direction} is translated directly to the local face, in short north -> north, south -> south ...
     * But with the free rotation block it is different, because the extra rotation {@link FreeRotationBlock#ROTATION} the local and global can't be translated directly.<br>
     * What this method does is use a global direction and apply the rotation to get the direction that the face is pointing,<i>assuming that the NORTH and NONE rotation is the default block state (no rotation applied)</i>.
     */
    public static Direction APPLY_STATE_ROTATION_IN_GLOBAL_DIRECTION(BlockState state, Direction global) {
        Direction facing = state.getValue(FACING);
        Rotation rotation = state.getValue(ROTATION);

        //makes that "facing" points to the north to match with the default block state.
        global = switch (facing)
        {
            case NORTH -> global;
            case SOUTH -> global.getClockWise(Direction.Axis.Y).getClockWise(Direction.Axis.Y);
            case WEST -> global.getClockWise(Direction.Axis.Y);
            case EAST -> global.getCounterClockWise(Direction.Axis.Y);
            case UP -> global.getClockWise(Direction.Axis.X);
            case DOWN -> global.getCounterClockWise(Direction.Axis.X);
        };

        return switch (rotation)//only apply the rotation around the ZN axis "north" to get the final direction.
        {
            case NONE -> global;
            case CLOCKWISE_90 -> global.getClockWise(Direction.Axis.Z);
            case CLOCKWISE_180 -> global.getClockWise(Direction.Axis.Z).getClockWise(Direction.Axis.Z);
            case COUNTERCLOCKWISE_90 -> global.getCounterClockWise(Direction.Axis.Z);
        };
    }
}
