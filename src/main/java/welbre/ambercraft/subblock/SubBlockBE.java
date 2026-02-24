package welbre.ambercraft.subblock;

import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
        TinyBlockState last = tinyBS.getLast();//don't check for null; We know what we are doing.
        final var newest = last.definition.shape.bounds().move(last.x / 16.0, last.y / 16.0, last.z / 16.0);

        final int size = tinyBS.size() - 1;
        {
            for (int i = 0; i < size; i++)
            {
                AABB state = tinyBS.get(i).definition.shape.bounds().move(tinyBS.get(i).x / 16.0, tinyBS.get(i).y / 16.0, tinyBS.get(i).z / 16.0);

                //check computes the minimal distance between the min and a max for each axe.
                Direction dpx = Direction.EAST;
                double touchX = -1;
                if ( (state.minX >= newest.minX && state.minX <= newest.maxX))
                    touchX = newest.maxX - state.minX;
                else if ((state.maxX >= newest.minX && state.maxX <= newest.maxX))
                {
                    touchX = state.maxX - newest.minX;
                    dpx = Direction.WEST;
                }

                Direction dpy = Direction.UP;
                double touchY = -1;
                if ( (state.minY >= newest.minY && state.minY <= newest.maxY))
                    touchY = newest.maxY - state.minY;
                else if ((state.maxY >= newest.minY && state.maxY <= newest.maxY))
                {
                    touchY = state.maxY - newest.minY;
                    dpy = Direction.DOWN;
                }

                Direction dpz = Direction.SOUTH;
                double touchZ = -1;
                if ( (state.minZ >= newest.minZ && state.minZ <= newest.maxZ))
                    touchZ = newest.maxZ - state.minZ;
                else if ((state.maxZ >= newest.minZ && state.maxZ <= newest.maxZ))
                {
                    touchZ = state.maxZ - newest.minZ;
                    dpz = Direction.NORTH;
                }

                boolean touch = false;
                Direction touchDir = null;
                //very hard to understand
                //if x and y have collision in the axes, and the z distance is zero, then is side by side in the Z axes.
                //if x and z have collision in the axes, and the y distance is zero, then is side by side in the Y axes.
                //if y and z have collision in the axes, and the x distance is zero, then is side by side in the X axes.
                if (touchX != 0 && touchY != 0 && touchZ == 0) {//z
                    touchDir = dpz;
                    touch = true;
                }
                else if(touchX != 0 && touchY == 0 && touchZ != 0){//y
                    touchDir = dpy;
                    touch = true;
                }
                else if (touchX == 0 && touchY != 0 && touchZ != 0){//x
                    touchDir = dpx;
                    touch = true;
                }

                if (!touch)
                    System.out.println("Isn't side by side dist");
                else
                    System.out.println("Is side by side collude at: " + touchDir.getName());
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

        setChanged();
        requestModelDataUpdate();
        if (level != null)
            //todo check if is working in the multiplayer.
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL_IMMEDIATE);//forces the re-rendering of the block, requiring a new BakedModel

        updateAround();

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
