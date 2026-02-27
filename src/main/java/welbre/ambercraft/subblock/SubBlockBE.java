package welbre.ambercraft.subblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
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

    //region TinyBlockState management
    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------TinyBlockState-------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    /// Reset to the default state
    protected void reset()
    {
        tinyBS.clear();
        shape = Shapes.empty();
    }

    /**
     * Update the neighbor / external contact from the last state in the tinyBs. <br>
     * Call this only after add a new a fresh state in the subBlock,
     * this method is sensitive to multiple calls and can set the SubBlockBE to an invalid state!!!
     */
    protected void updateAround()
    {
        final TinyBlockState last = tinyBS.getLast();//don't check for null; We know what we are doing.
        final var model = last.getTranslatedBounds();

        List<TinyBlockState> candidates = new ArrayList<>();
        //filter for only states that is inside or side by side with the last.
        {
            //inflate in block( 1/16) in all distances, and check for clip
            final AABB inflated = model.inflate(1 / 16.0, 1 / 16.0, 1 / 16.0);

            //external contact check
            if (inflated.minX < 0)
                last.externalContact.add(Direction.WEST);
            if (inflated.maxX > 1)
                last.externalContact.add(Direction.EAST);
            if (inflated.minY < 0)
                last.externalContact.add(Direction.DOWN);
            if (inflated.maxY > 1)
                last.externalContact.add(Direction.UP);
            if (inflated.minZ < 0)
                last.externalContact.add(Direction.NORTH);
            if (inflated.maxZ > 1)
                last.externalContact.add(Direction.SOUTH);

            //block occlusion check
            for (Direction dir : Direction.values())
                if (getLevel() != null && !Block.shouldRenderFace(getLevel(), getBlockPos(), getBlockState(), getLevel().getBlockState(getBlockPos().relative(dir)), dir))
                    last.fullOccluded.put(dir, null);

            for (int i = 0; i < tinyBS.size() - 1; i++)
            {
                AABB other = tinyBS.get(i).getTranslatedBounds();
                if (inflated.intersects(other))
                    candidates.add(tinyBS.get(i));
            }
        }

        if (candidates.isEmpty())
            return;

        for (Direction dir : Direction.values())
        {
            if (last.externalContact.contains(dir))//if the face is external, skip.
                continue;

            AABB faceBox = switch (dir)
            {
                case DOWN -> new AABB(model.minX, model.minY - 1/16.0, model.minZ, model.maxX, model.minY, model.maxZ);
                case UP -> new AABB(model.minX, model.maxY, model.minZ, model.maxX, model.maxY + 1/16.0, model.maxZ);
                case WEST -> new AABB(model.minX - 1/16.0, model.minY, model.minZ, model.minX, model.maxY, model.maxZ);
                case EAST -> new AABB(model.maxX, model.minY, model.minZ, model.maxX + 1/16.0, model.maxY, model.maxZ);
                case NORTH -> new AABB(model.minX, model.minY, model.minZ - 1/16.0, model.maxX, model.maxY, model.minZ);
                case SOUTH -> new AABB(model.minX, model.minY, model.maxZ, model.maxX, model.maxY, model.maxZ + 1/16.0);
            };
            for (TinyBlockState state : candidates)//we already know that this is side by side.
            {
                AABB box = state.getTranslatedBounds();
                if (box.intersects(faceBox))//checks if the face and the box are interacting
                {
                    if (box.intersect(faceBox).equals(faceBox))//full occlusion check
                    {
                        last.fullOccluded.put(Direction.WEST, state);
                        state.fullOccluded.put(Direction.WEST, last);
                    }

                    last.addNeighbor(Direction.WEST, state);
                    state.addNeighbor(Direction.WEST, last);
                }
            }
        }
    }

    /**
     * Adds a new {@link TinyBlockState} in the SubBlock using the tinyBlock
     * @param tinyBlock The TinyBlock type
     * @param x the x coordinate in a 0 to 15 scale
     * @param y the y coordinate in a 0 to 15 scale
     * @param z the z coordinate in a 0 to 15 scale
     * @return if the tiny state has been placed.
     */
    public boolean addTinyBlock(@NotNull TinyBlock tinyBlock, final int x, final int y, final int z)
    {
        tinyBS.add(new TinyBlockState(tinyBlock, x, y, z));
        shape = Shapes.empty();
        for (TinyBlockState state : tinyBS)
            shape = Shapes.or(shape, state.definition.shape.move(state.x / 16.0, state.y/16.0, state.z/16.0));

        updateAround();

        setChanged();
        requestModelDataUpdate();
        if (level != null)
            //todo check if is working in the multiplayer.
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL_IMMEDIATE);//forces the re-rendering of the block, requiring a new BakedModel

        return true;
    }

    /// Update the internal model of <b>one specific</b> state
    /// @param state The state to be updated
    public void requireStaticRenderUpdate(TinyBlockState state)
    {
        //todo implement it
    }
    /// Update all the internal models.
    public void requireStaticRenderUpdate()
    {
        //todo implement it
    }

    /**
     * Checks if a tinyBlock can be placed at the x,y,z position in the SubBlock.
     * @param x spects a coordinate between 0 and 16, related to the subSpace
     * @param y spects a coordinate between 0 and 16, related to the subSpace
     * @param z spects a coordinate between 0 and 16, related to the subSpace
     */
    public boolean canPlace(@NotNull TinyBlock tinyBlock, final int x, final int y, final int z)
    {
        //todo re implement it, check internal conflicts for space
        AABB moved = tinyBlock.shape.bounds().move(x / 16f, y / 16f, z / 16f);
        return  moved.maxX <= 1 && moved.maxY <= 1 && moved.maxZ <= 1 && moved.minX >= 0 && moved.minY >= 0 && moved.minZ >= 0;
    }

    /// Drops a tiny block from the subBlock, notice that the dropped item is defined by the {@link TinyBlock#getDroppedItem()} not by this method.
    public void dropTinyState(TinyBlockState state)
    {
        if (level == null)
            return;

        if (tinyBS.remove(state))
        {
            ItemStack droppedItem = state.definition.getDroppedItem();
            if (droppedItem != null)
            {
                var itemEntity = new ItemEntity(level, state.x / 16f + getBlockPos().getX(), state.y / 16f + getBlockPos().getY(), state.z / 16f + getBlockPos().getZ(), droppedItem);
                level.addFreshEntity(itemEntity);
            }
        }

        setChanged();
        requestModelDataUpdate();
    }
    //endregion
    //region Data
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

                //checks if the added new state can be placed in the SubBlock
                if (!canPlace(state.definition, state.x, state.y, state.z))
                    dropTinyState(state);//first add and before check, because dropTinyState hopes that the state should be in the tinyBS
            }

            shape = Shapes.empty();
            //add all, and re math the shape
            for (TinyBlockState state : tinyBS)
                shape = Shapes.or(shape, state.definition.shape.move(state.x / 16.0, state.y/16.0, state.z/16.0));

            requestModelDataUpdate();
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
        //info send by the sever to the client updates the block entity
        var x = new CompoundTag();
        saveAdditional(x, registries);
        return x;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider lookupProvider) {
        //the update process from the getUpdateTag
        super.handleUpdateTag(tag, lookupProvider);
    }

    //endregion

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
