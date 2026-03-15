package welbre.ambercraft.subblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/// Is a "shadow", used to warp another state and share the propriety to other SubBlock
public class SharedTinyBlockState extends TinyBlockState
{
    /// The SubBlockBE that owner the original state.
    private BlockPos owner;
    /// The SubBlockBE that owner the shadow state.
    private BlockPos getter;
    /// Where in the {@link SubBlockBE#tinyBS} the "original" state can be founded
    private int index;

    /**
     *
     * @param state The state to be shadow to.
     * @param owner The owner of the state.
     * @param shared The SubBlockBE that is sharing with.
     */
    public SharedTinyBlockState(@NotNull TinyBlockState state, @NotNull SubBlockBE owner, @NotNull SubBlockBE shared)
    {
        super(state.definition, state.x,state.y,state.z);
        this.index = owner.tinyBS.indexOf(state);
        if (this.index == -1)
            throw new RuntimeException("Tried to create a SharedTinyBlockState with a state that is not in the owner SubBlockBE!");
        this.owner = owner.getBlockPos();
        this.getter = shared.getBlockPos();
    }

    public SharedTinyBlockState()
    {
    }


    public @NotNull BlockPos getOwner() {
        return owner;
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
        tag.putShort("index", (short) index);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag tag) {
        owner = BlockPos.of(tag.getLong("owner"));
        getter = BlockPos.of(tag.getLong("getter"));
        index = tag.getShort("index");
        super.deserializeNBT(provider, tag);
    }

    @Override
    public String toString()
    {
        return "SharedTinyBlockState(%s at %d %d %d got by %d %d %d owner by %d %d %d at %d index)"
                .formatted(definition.registerName, x, y, z, getter.getX(), getter.getY(), getter.getZ(), owner.getX(), owner.getY(), owner.getZ(), index);
    }
}
