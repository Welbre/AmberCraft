package welbre.ambercraft.subblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to all internal logic behind the {@link TinyBlock}.<br>
 * If you want to create your own TinyBlocks, se him documentation.
 */
public class SubBlockBE extends BlockEntity
{
    /// Used in the baked model
    public static final ModelProperty<List<TinyBlockState>> TINY_BLOCK_STATE_MODEL_PROPERTY = new ModelProperty<>();

    protected final List<TinyBlockState> tinyBS = new ArrayList<>();
    protected VoxelShape shape = Shapes.empty();


    public SubBlockBE(BlockPos pos, BlockState blockState) {
        super(AmberCraft.BlockEntity.SUB_BLOCK_BE.get(), pos, blockState);
    }

    /// Reset to the default state
    protected void reset()
    {
        tinyBS.clear();
        shape = Shapes.empty();
    }

    public void addTinyBlock(@NotNull TinyBlock tinyBlock, final int x, final int y, final int z)
    {
        tinyBS.add(new TinyBlockState(tinyBlock, x, y, z));
        shape = Shapes.empty();
        for (TinyBlockState state : tinyBS)
            shape = Shapes.or(shape, state.definition.shape);
    }

    /**
     * Checks if a tinyBlock can be placed at the x,y,z position in the SubBlock.
     */
    public boolean canPlace(@NotNull TinyBlock tinyBlock, final int x, final int y, final int z)
    {
        AABB moved = tinyBlock.shape.bounds().move(x, y, z);
        return  moved.maxX <= 1 && moved.maxY <= 1 && moved.maxZ <= 1 && moved.minX >= 0 && moved.minY >= 0 && moved.minZ >= 0;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------Data-----------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
    {
        super.loadAdditional(tag, registries);
        reset();
        //load tiny block state
        if (tag.contains("tbs"))
        {
            var tbs = tag.getCompound("tbs");
            int size = tbs.getInt("size");
            for (int i = 0; i < size; i++)
            {
                var state = new TinyBlockState();
                tinyBS.add(state);
                state.deserializeNBT(registries, tbs.getCompound(String.valueOf(i)));
            }
            //add all, and re math the shape
            for (TinyBlockState state : tinyBS)
                shape = Shapes.or(shape, state.definition.shape.move(state.x / 16.0, state.y/16.0, state.z/16.0));
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
    {
        super.saveAdditional(tag, registries);
        {
            var tbs = new CompoundTag();
            tbs.putInt("size", tinyBS.size());
            //serialize all tbs in an array format
            for (int i = 0; i < tinyBS.size(); i++)
            {
                tbs.put(String.valueOf(i), tinyBS.get(i).serializeNBT(registries));
            }
            tag.put("tbs", tbs);//tiny block state
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        //info send by the sever to the client update the block entity
        var x = new CompoundTag();
        saveAdditional(x, registries);
        return x;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider lookupProvider) {
        //the update process from the getUpdateTag
        super.handleUpdateTag(tag, lookupProvider);
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------Getters--------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public @NotNull ModelData getModelData()
    {
        return ModelData.builder().with(TINY_BLOCK_STATE_MODEL_PROPERTY, tinyBS).build();
    }

    protected @NotNull VoxelShape shape(){return shape;}
}
