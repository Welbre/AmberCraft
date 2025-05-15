package welbre.ambercraft.blocks.parent;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AmberFreeBlock extends AmberSidedBasicBlock {
    public static final EnumProperty<FaceRotation> ROTATION = EnumProperty.create("face_rotation", FaceRotation.class, FaceRotation.UP, FaceRotation.LEFT, FaceRotation.DOWN, FaceRotation.RIGHT);

    public AmberFreeBlock(Properties p) {
        super(p);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(ROTATION, FaceRotation.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ROTATION);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        //todo create a method to placement
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }



    /**
     * Uses an anti_clock_wise notation to represent a cube rotation.
     */
    public enum FaceRotation implements StringRepresentable {
        UP("0"),
        LEFT("90"),
        DOWN("180"),
        RIGHT("270");

        final String degree;

        FaceRotation(String degree) {
            this.degree = degree;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.degree;
        }
    }
}
