package welbre.ambercraft.subblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Is the main block used by ambercraft cables and pipes; Roughly is a 16 * 16 * 16 block, that {@link TinyBlock} can be added to it.<br>
 * All rending, logic, events is handled by the {@link SubBlockBE}. To achieve this, all logic is allocated in the BlockEntity due to the minecraft
 * rigidity in the Block/BlockState system.
 */
public class SubBlock extends Block implements EntityBlock
{
    public SubBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context)
    {
        if (level.getBlockEntity(pos) instanceof SubBlockBE be)
            return be.shape();
        else
            return Shapes.empty();
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new SubBlockBE(pos, state);
    }
}
