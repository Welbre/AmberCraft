package welbre.ambercraft.subblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;

import java.util.*;
import java.util.function.Consumer;

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

        //update occlusion and neighbor
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
                        last.fullOccluded.put(dir, state);
                        state.fullOccluded.put(dir.getOpposite(), last);//todo fix, this should be wrong :(
                    }

                    last.addNeighbor(dir, state);
                    state.addNeighbor(dir.getOpposite(), last);
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
        if (!canPlace(tinyBlock, x, y, z))
            return false;
        tinyBS.add(new TinyBlockState(tinyBlock, x, y, z));
        shape = Shapes.empty();
        for (TinyBlockState state : tinyBS)
            shape = Shapes.or(shape, state.definition.shape.move(state.x / 16.0, state.y/16.0, state.z/16.0));

        updateAround();

        setChanged();
        requestModelDataUpdate();
        if (level != null)
            //todo check if is working in the multiplayer. Microsoft sucks a lot.
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
        AABB moved = tinyBlock.shape.bounds().move(x / 16f, y / 16f, z / 16f);
        if (moved.maxX > 1 || moved.maxY > 1 || moved.maxZ > 1 || moved.minX < 0 || moved.minY < 0 || moved.minZ < 0)
            return false;

        List<TinyBlockState> states = new ArrayList<>(List.of(new TinyBlockState(tinyBlock, x, y, z)));
        states.addAll(tinyBS);

        //collision check
        final int size = states.size();
        for (int i = 0; i < size - 1; i++)
        {
            final var shape_a = states.get(i).getTranslatedAABB();
            for (int j = i + 1; j < size; j++)
            {
                final var shape_b = states.get(j).getTranslatedAABB();
                for (AABB aabb_a : shape_a)
                    for (AABB aabb_b : shape_b)
                        if (aabb_a.intersects(aabb_b))
                            return false;
            }
        }

        return true;
    }

    /// Drops a tiny block from the subBlock, notice that the dropped item is defined by the {@link TinyBlock#getDroppedItem(TinyBlockState, LootParams.Builder)} not by this method.
    public void dropTinyState(TinyBlockState state)
    {
        if (level == null)
            return;

        if (tinyBS.remove(state))
        {
            ItemStack droppedItem = state.definition.getDroppedItem(state, null);
            if (droppedItem != null)
            {
                var itemEntity = new ItemEntity(level, state.x / 16f + getBlockPos().getX(), state.y / 16f + getBlockPos().getY(), state.z / 16f + getBlockPos().getZ(), droppedItem);
                level.addFreshEntity(itemEntity);
            }
            setChanged();
            requestModelDataUpdate();
        }
    }
    //endregion
    //region Data
    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------Data-----------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    /// A system to deal with memory reference to TinyBlockStates
    protected static final class TBSReference
    {
        private record REQUEST(int hash, Consumer<TinyBlockState> consumer){}

        private static final Map<Integer, TinyBlockState> BATCH = new HashMap<>();
        private static final List<REQUEST> REQUESTS = new ArrayList<>();

        private TBSReference(){}

        /// Used to solve memory references to TinyBLockState, the consumer receives a TinyBlockState compatible with the position.
        public static void SOLVE(int pos, Consumer<TinyBlockState> consumer)
        {
            if (pos == -1)
                consumer.accept(null);
            else
                REQUESTS.add(new REQUEST(pos, consumer));
        }

        /// Used to solve memory references to TinyBLockState, the consumer receives all TinyBlockState compatible with the position.
        public static void SOLVE(int[] pos, Consumer<TinyBlockState> consumer)
        {
            for (int coordinate : pos)
                SOLVE(coordinate, consumer);
        }

        /// Resolve all memory reference requirements
        public static void SOLVE_REQUESTS(@NotNull List<@NotNull TinyBlockState> states)
        {
            for (REQUEST request : REQUESTS)
            {
                TinyBlockState state = BATCH.get(request.hash);
                if (state != null)
                    request.consumer.accept(state);
            }
            REQUESTS.clear();
        }

        /// Clear up all, check inconsistency and initialize the BATCH.
        public static void BEGIN_BATCH(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries, List<TinyBlockState> states)
        {
            BATCH.clear();
            states.forEach(state -> BATCH.put(state.getCompactedPosition(), state));

            if (tag.contains("tbs_cache"))
            {
                int[] cords = tag.getIntArray("tbs_cache");
                int match = 0;
                for (int i : cords)
                    if (BATCH.containsKey(i))
                        match++;

                if (match != BATCH.size())
                {
                    BATCH.clear();
                    throw new RuntimeException("The cache is corrupted (BATCH: %d, REQUIRED: %d, MATCH: %d), please report this to the developer.".formatted(BATCH.size(), cords.length, match));
                }
            }
        }

        /// Serialize all tbs hashes
        public static void SAVE_BATCH(@NotNull CompoundTag tag, HolderLookup.Provider registries, List<TinyBlockState> states)
        {
            tag.putIntArray("tbs_cache", states.stream().map(TinyBlockState::getCompactedPosition).toList());
        }
    }

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
                state.deserializeNBT(registries, tbs.getCompound(String.valueOf(i)));

                //checks if the added new state can be placed in the SubBlock
                if (canPlace(state.definition, state.x, state.y, state.z))
                    tinyBS.add(state);
                else
                    dropTinyState(state);
            }
            TBSReference.BEGIN_BATCH(tag, registries, this.tinyBS);
            TBSReference.SOLVE_REQUESTS(tinyBS);


            shape = Shapes.empty();
            //add all, and re math the shape
            for (TinyBlockState state : tinyBS)
                shape = Shapes.or(shape, state.getTranslatedShape());

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
                tbs.put(String.valueOf(i), tinyBS.get(i).serializeNBT(registries));
            tag.put("tbs", tbs);//tiny block state
        }
        TBSReference.SAVE_BATCH(tag, registries, this.tinyBS);
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

    public Collection<TinyBlockState> getTinyStates() {
        return tinyBS;
    }

    protected @NotNull VoxelShape shape(){return shape;}

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------Helpers/extras-------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Used to get the aiming TinyBlockState in a SubBloc,
     * This method uses the {@link Player#pick(double, float, boolean)} method to pick a block in the player range;
     * @param player The player to use to the RayCast.
     * @return Returns the aimed TinyBlockState or null if don't find one.
     */
    public @Nullable TinyBlockState getStateByRayCast(Player player)
    {
        HitResult pick = player.pick(Math.pow(player.blockInteractionRange(), 2), 1F, false);
        if (pick instanceof BlockHitResult result)
        {
            //grid location in 0 to 1 scale
            Vec3 g = pick.getLocation().subtract(result.getBlockPos().getX(), result.getBlockPos().getY(), result.getBlockPos().getZ());

            for (TinyBlockState state : getTinyStates())
                for (AABB aabb : state.getTranslatedAABB())
                    if (g.x >= aabb.minX && g.x <= aabb.maxX && g.y >= aabb.minY && g.y <= aabb.maxY && g.z >= aabb.minZ && g.z <= aabb.maxZ)
                        return state;
        }
        return null;
    }
}
