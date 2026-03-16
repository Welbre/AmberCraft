package welbre.ambercraft.subblock;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.model.EmptyModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/// Is a "shadow", used to warp another state and share the propriety to other SubBlock
public class SharedTinyBlockState extends TinyBlockState
{
    /// The SubBlockBE that owner of the original state.
    private BlockPos owner;
    /// The SubBlockBE that owner of the shadow state.
    private BlockPos getter;

    /**
     *
     * @param state The state to be shadow to.
     * @param owner The owner of the state.
     * @param shared The SubBlockBE that is sharing with.
     */
    public SharedTinyBlockState(@NotNull TinyBlockState state, @NotNull SubBlockBE owner, @NotNull SubBlockBE shared)
    {
        super(state.definition, state.x,state.y,state.z);
        {
            boolean find = false;
            for (TinyBlockState blockState : owner.tinyBS)
            {
                if (blockState == state)
                {
                    find = true;
                    break;
                }
            }

            if (!find)
                throw new RuntimeException("Tried to create a SharedTinyBlockState with a state that is not in the owner SubBlockBE!");
        }
        this.owner = owner.getBlockPos();
        this.getter = shared.getBlockPos();
    }

    public SharedTinyBlockState()
    {
    }

    @Override
    public BakedModel staticModel(TinyBlockState blockState)
    {
        return EmptyModel.BAKED;
    }

    public @NotNull BlockPos getOwner()
    {
        return owner;
    }

    public BlockPos getGetter()
    {
        return getter;
    }

    public @NotNull TinyBlockState getOriginalState(@NotNull Level level)
    {
        if (level.getBlockEntity(owner) instanceof SubBlockBE ownerBE)
        {
            for (TinyBlockState state : ownerBE.tinyBS)
                if (state.x == this.x && state.y == this.y && state.z == this.z)
                    return state;

            throw new IllegalStateException("Shared TinyBlockState with owner:%s at index:(%d,%d,%d) can't be found because SubBlockBE don't contains the index!".formatted(owner, x,y,z));
        }
        else
            throw new IllegalStateException("Shared TinyBlockState with owner:%s at index:(%d,%d,%d) can't be found because owner isn't a SubBlock!".formatted(owner, x,y,z));
    }

    @Override
    public @NotNull AABB getTranslatedBounds()
    {
        return getTranslatedShape().bounds();
    }

    @Override
    public @NotNull VoxelShape getTranslatedShape()
    {
        //The same but with the owner as origin
        VoxelShape shape = super.getTranslatedShape();
        BlockPos diff = owner.subtract(getter);//vector to move from this position to the owner position

        return shape.move(diff.getX(), diff.getY(), diff.getZ());
    }

    @Override
    public @NotNull List<AABB> getTranslatedAABB()
    {
        return getTranslatedShape().toAabbs();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider)
    {
        CompoundTag tag = super.serializeNBT(provider);
        tag.putLong("owner", owner.asLong());
        tag.putLong("getter", getter.asLong());
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag tag) {
        owner = BlockPos.of(tag.getLong("owner"));
        getter = BlockPos.of(tag.getLong("getter"));
        super.deserializeNBT(provider, tag);
    }

    @Override
    public String toString()
    {
        return "SharedTinyBlockState(%s tbs owned by (%d,%d,%d) and shared with (%d,%d,%d)"
                .formatted(definition.registerName, owner.getX(), owner.getY(), owner.getZ(), getter.getX(), getter.getY(), getter.getZ());
    }
}
