package welbre.ambercraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is a helper to make the used off free block less painful :(.<br>
 * Used for each AmberCraft block that can face any of 6 direction and rotate around the facing direction.<br>
 * So they are free to rotate around any axes. Uses a total of 24 block states, 6 (facing) * 4 (rotation around face) = 12 states.<br>
 */
public final class FreeRotationBlockHelper
{
    /// A set of itens that can be used to rotate the block.
    public static final Set<Item> WRENCHES = new HashSet<>();
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final EnumProperty<Rotation> ROTATION = EnumProperty.create("rotation", Rotation.class);

    /**
     * Call it in your block class constructor!, this will set default FACING and the ROTATION in the block.
     * <br>
     * Exemple:
     * <pre>
     *     {@code
     *         public MyBlock(Properties p)
     *         {
     *             super(p);
     *             FreeRotationBlockHelper.REGISTER_DEFAULT_STATE(this);
     *             ...
     *         }
     *     }
     * </pre>
     * @param block you block singleton.
     */
    public static void REGISTER_DEFAULT_STATE(@NotNull Block block)
    {
        try
        {
            Method createBlockStateDefinition = block.getClass().getDeclaredMethod("registerDefaultState", BlockState.class);
            BlockState state = block.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(ROTATION, Rotation.NONE);
            createBlockStateDefinition.invoke(block, state);

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Call it in your {@link Block#createBlockStateDefinition}!,this will include rotation and facing in the block state definition
     * <br>
     * Exemple:
     * <pre>
     *     {@code
     *          @Override
     *           protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
     *               super.createBlockStateDefinition(builder);
     *               FreeRotationBlockHelper.CREATE_BLOCK_STATE_DEFINITION(builder);
     *           }
     *     }
     * </pre>
     */
    public static void CREATE_BLOCK_STATE_DEFINITION(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
        builder.add(ROTATION);
    }

    /**
     * Call it in your {@link Block#getStateForPlacement(BlockPlaceContext)}!,this whill handles how the rotation and facing should be set based on how the player put the block.
     * <br>
     * Exemple:
     * <pre>
     *     {@code
     *     @Override
     *     public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
     *         return FreeRotationBlockHelper.GET_STATE_FOR_PLACEMENT(this, context);
     *     }
     *     }
     * </pre>
     */
    public static @NotNull BlockState GET_STATE_FOR_PLACEMENT(@NotNull Block block, @NotNull BlockPlaceContext context)
    {
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
        return block.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite()).setValue(ROTATION, rotation);
    }

    /**
     * Call it in your {@link Block#useItemOn}!,this whill handles block rotation using an item by the player.<br>
     * <b>To add a new wrench put your desired item in {@link FreeRotationBlockHelper#ROTATION} set <a color="red">AND</a> in your item class make sure that
     * {@link Item#useOn(UseOnContext)} returns a {@link InteractionResult#SUCCESS} if the item have handled the use with success,
     * else, the rotation doesn't happen.
     * So in the useOn method you can check for energy or other thing that can affect you tool.</b>
     * <br>
     * Exemple (this is a bit complicated):
     * <pre>
     *     {@code
     *         @Override
     *         protected @NotNull InteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,@NotNull Player player,@NotNull InteractionHand hand,@NotNull BlockHitResult hitResult)
     *         {
     *              InteractionResult result = FreeRotationBlockHelper.USE_ITEM_ON(stack, state, level, pos, player, hand, hitResult);
     *              if (result.consumesAction())
     *                  return result;
     *              ...
     *         }
     * </pre>
     */
    public static @NotNull InteractionResult USE_ITEM_ON(
            @NotNull ItemStack stack,
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull Player player,
            @NotNull InteractionHand hand,
            @NotNull BlockHitResult hitResult)
    {
        if (hitResult.getDirection() == state.getValue(FACING) && player.getMainHandItem().is(Items.AIR))
        {
            for (Item wrench : WRENCHES)
            {
                //the result depends on the item too... so run the useOn to the item, try to handle the click,
                //and if the usage is a success, then perform the rotation
                InteractionResult result = wrench.useOn(new UseOnContext(level, player, hand, stack, hitResult));
                if (result.consumesAction())
                {
                    var newState = state.setValue(ROTATION, state.getValue(ROTATION).getRotated(Rotation.CLOCKWISE_90));
                    level.setBlock(pos, newState, 3);
                }
                return result;
            }
        }
        return InteractionResult.PASS;
    }

    /**
     * This computes the <code>local</code> face direction of a <code>global</code> direction using the state.<br>
     * In a block that can't be rotated, a global {@link Direction} is translated directly to the local face, in short north -> north, south -> south ...
     * But with the free rotation block it is different, because the extra rotation {@link FreeRotationBlockHelper#ROTATION} the local and global can't be translated directly.<br>
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
