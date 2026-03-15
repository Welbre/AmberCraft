package welbre.ambercraft.subblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

/**
 * Used when a {@link TinyBlockState} is shared across multiples {@link SubBlock}.
 */
public record SharedTBS(@NotNull BlockPos owner, @NotNull Level level, int index)
{
    public TinyBlockState state()
    {
        if (level.getBlockEntity(owner) instanceof SubBlockBE sub)
            if (index < sub.tinyBS.size())
                if (sub.tinyBS.get(index) != null)
                    return sub.tinyBS.get(index);
                else
                    throw new IllegalStateException("Shared TinyBlockState with owner:%s at index:%d can't be found because the TinyBlockState ate this index is null!".formatted(owner, index));
            else
                throw new IllegalStateException("Shared TinyBlockState with owner:%s at index:%d can't be found because SubBlockBE don't contains the index!".formatted(owner, index));
        else
            throw new IllegalStateException("Shared TinyBlockState with owner:%s at index:%d can't be found because owner isn't a SubBlock!".formatted(owner, index));
    }

    /// Get the shape of the state using the getter as origin
    public VoxelShape getTranslatedShape(@NotNull BlockPos getter)
    {
        //The same but with the owner as origin
        VoxelShape voxelShape = state().getTranslatedShape();//zero on the same of the owner
        BlockPos diff = owner().subtract(getter);//vector to move from this position to the owner position

        return voxelShape.move(diff.getX(), diff.getY(), diff.getZ());
    }

    public static @NotNull CompoundTag serializeNBT(SharedTBS sharedTBS, HolderLookup.@NotNull Provider ignoredProvider)
    {
        CompoundTag tag = new CompoundTag();

        tag.putLong("pos", sharedTBS.owner.asLong());
        tag.putInt("index", sharedTBS.index);

        return tag;
    }

    public static @NotNull SharedTBS deserializeNBT(HolderLookup.@NotNull Provider ignoredProvider, @NotNull CompoundTag nbt, @NotNull Level level)
    {
        return new SharedTBS(BlockPos.of(nbt.getLong("pos")), level, nbt.getInt("index"));
    }
}
