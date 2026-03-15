package welbre.ambercraft.subblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
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

    /// Used in the breaking pipeline
    private @Nullable TinyBlockState playerIsBreaking = null;

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

    protected void updateShape()
    {
        shape = Shapes.empty();
        for (TinyBlockState state : tinyBS)
            shape = Shapes.or(shape, Shapes.join(Shapes.block(), state.getTranslatedShape(), BooleanOp.AND));//Join the shape and the intersection between shape and block bounds
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
                    }

                    last.addNeighbor(dir, state);
                    state.addNeighbor(dir.getOpposite(), last);
                }
            }
        }
    }

    /**
     * Used in the SubBlock to update all Occlusion data when a neighbor change.
     * @param direction The directions to update
     */
    void updateOcclusion(Direction... direction)
    {
        for (var dir : direction)
            //clear the direction if is null
            for (var state : tinyBS)
                //if isn't occluded by other TinyBlockState (fullOccluded don't contain the key) or is occluded by block (fullOccluded contain the key, but it is null)
                if (state.fullOccluded.get(dir) == null)
                    if (getLevel() != null && Block.shouldRenderFace(getLevel(), getBlockPos(), getBlockState(), getLevel().getBlockState(getBlockPos().relative(dir)), dir))
                        state.fullOccluded.remove(dir);//mark the face to be rendered
                    else
                        state.fullOccluded.put(dir, null);//skip render this face

        synchronize();
    }

    /**
     * Adds a new {@link TinyBlockState} in the SubBlock using the tinyBlock
     * @param tinyBlock The TinyBlock type
     * @param grid Placement context
     * @return if the tiny state has been placed.
     */
    public boolean addTinyBlock(@NotNull TinyBlock tinyBlock, final Grid16Context grid)
    {
        if (level == null | !canPlace(tinyBlock, grid))
            return false;
        tinyBS.add(new TinyBlockState(tinyBlock, grid.x(), grid.y(), grid.z()));

        if (grid.hasShared())//if is shared, then create all absent SubBlock
            for (BlockPos shared : grid.shared())
                if (!level.getBlockState(shared).is(AmberCraft.Blocks.SUB_BLOCK))
                {
                    level.setBlockAndUpdate(shared, AmberCraft.Blocks.SUB_BLOCK.get().defaultBlockState());

                    if (level.getBlockEntity(shared) instanceof SubBlockBE shareBE)
                        shareBE.tinyBS.add(new SharedTinyBlockState(tinyBS.getLast(), this, shareBE));
                }

        updateShape();
        updateAround();
        synchronize();

        return true;
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
        return addTinyBlock(tinyBlock, new Grid16Context(x, y, z, getBlockPos(), Grid16Context.GET_SHARED_LIST(tinyBlock, getBlockPos(), new Vec3i(x, y, z))));
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
        return canPlace(tinyBlock, new Grid16Context(x,y,z, getBlockPos(), Grid16Context.GET_SHARED_LIST(tinyBlock, getBlockPos(), new Vec3i(x, y, z))));
    }

    /**
     * Checks if a tinyBlock can be placed in the SubBlock using a grid context.
     * @param context The context placement
     */
    public boolean canPlace(@NotNull TinyBlock tinyBlock, final Grid16Context context)
    {
        if (level == null)
            return false;

        List<AABB> aabb = tinyBlock.getTranslatedAABB(new TinyBlockState(tinyBlock, context.x(), context.y(), context.z()));

        //check for collision between the TinyBlock that will be placed and the TinyBlockState already in place in the anchor
        if (this.collisionCheck(aabb))
            return false;

        //similar but check in all shared in the context and for block in the surround
        for (BlockPos sharedPos : context.shared())
            if (level.getBlockEntity(sharedPos) instanceof SubBlockBE sharedBE)
            {
                if (sharedBE.collisionCheck(aabb))
                    return false;
            }
            else//if isn't a SubBlock, check if was collision in shapes
            {
                BlockState state = level.getBlockState(sharedPos);
                if (state.isAir())
                    continue;

                //move the state using the anchor as origin
                VoxelShape blockShape = state.getShape(level, sharedPos).move(
                        new Vec3(
                                sharedPos.getX() - context.anchor().getX(),
                                sharedPos.getY() - context.anchor().getY(),
                                sharedPos.getZ() - context.anchor().getZ()
                        )
                );
                for (AABB boxA : blockShape.toAabbs())
                    for (AABB boxB : aabb)
                        if (boxA.intersects(boxB))
                            return false;
            }


        return true;
    }

    /// Check collision between the aabb and all state in the SubBlockBE, the owned and the shared.
    /// @return if it has a collision.
    public boolean collisionCheck(@NotNull Collection<AABB> aabb)
    {
        ArrayList<TinyBlockState> list = new ArrayList<>(tinyBS);

        for (TinyBlockState state : list)
            for (AABB boxA : aabb)
                for (AABB boxB : state.getTranslatedAABB())
                    if (boxA.intersects(boxB))
                        return true;

        return false;
    }

    /// Drops a tiny block from the subBlock, notice that the dropped item is defined by the {@link TinyBlock#getDroppedItem(TinyBlockState, LootParams.Builder)} not by this method.
    public void dropTinyState(TinyBlockState state)
    {
        if (level == null)
            return;

        if (tinyBS.remove(state))
        {
            ItemStack droppedItem = state.getDroppedItem(state, null);
            if (droppedItem != null)
            {
                var itemEntity = new ItemEntity(level, state.getX() / 16f + getBlockPos().getX(), state.getY() / 16f + getBlockPos().getY(), state.getZ() / 16f + getBlockPos().getZ(), droppedItem);
                level.addFreshEntity(itemEntity);
            }

            if (tinyBS.isEmpty())//don't remove it, if the shape is empty, a crash will happen!
                return;
            
            updateShape();

            synchronize();
        }
    }

    /**
     * Breaks a TinyState from the SubBlock.<br> Dropping the loop if is in the right game mode.
     * @return If the SubBlock should be removed from the level
     */
    public boolean breakTinyState(@NotNull TinyBlockState state, @NotNull Player player, boolean willHarvest, @NotNull FluidState fluid)
    {
        if (level == null)
            return false;

        if (tinyBS.contains(state))
        {
            if (player.isCreative())
                tinyBS.remove(state);
            else
                dropTinyState(state);

            if (tinyBS.isEmpty())
                return true;

            updateShape();

            synchronize();
        }

        return false;
    }

    /**
     * Synchronizes the client model with the server version.<br><br>
     * The default pipeline uses level.sendBlockUpdated in method that runs in both sides so,
     * you can't send a {@link net.minecraft.client.renderer.LevelRenderer#setBlockDirty(BlockPos, BlockState, BlockState)} from the server,
     * and the default implementation checks for BlockState changes to send the packet,
     * that inhibit the SubBlock to update, duo the all data is stores in the BlockEntity.<br>
     */
    public void synchronize()
    {
        if (level != null)
        {
            //force a re-render in the SinglePlayer
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            if (level.isClientSide())
                requestModelDataUpdate();
            else
                setChanged();//this will send Packet later to update all data and model at once via network.
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
                    var er = new RuntimeException("The cache is corrupted (BATCH: %d, REQUIRED: %d, MATCH: %d), please report this to the developer.".formatted(BATCH.size(), cords.length, match));
                    BATCH.clear();
                    throw er;
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
        boolean shouldUpdateShape = false;

        //load tiny block state
        if (tag.contains("tbs"))
        {
            var tbs = tag.getCompound("tbs");
            int size = tbs.getInt("size");
            for (int i = 0; i < size; i++)
            {
                CompoundTag stateTag = tbs.getCompound(String.valueOf(i));

                TinyBlockState state = stateTag.getString("type").equals("shared") ? new SharedTinyBlockState() : new TinyBlockState();
                state.deserializeNBT(registries, stateTag.getCompound("data"));

                tinyBS.add(state);
            }
            TBSReference.BEGIN_BATCH(tag, registries, this.tinyBS);
            TBSReference.SOLVE_REQUESTS(tinyBS);

            shouldUpdateShape = true;
        }

        if (shouldUpdateShape)
        {
            updateShape();

            requestModelDataUpdate();
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
    {
        super.saveAdditional(tag, registries);
        {
            //tbs
            {
                var tbs = new CompoundTag();
                tbs.putInt("size", tinyBS.size());
                //serialize all tbs in an array format
                for (int i = 0; i < tinyBS.size(); i++)
                {
                    CompoundTag state = new CompoundTag();
                    state.putString("type", tinyBS.get(i) instanceof SharedTinyBlockState ? "shared" : "simple");
                    state.put("data", tinyBS.get(i).serializeNBT(registries));
                    tbs.put(String.valueOf(i), state);
                }
                tag.put("tbs", tbs);//tiny block state
            }
        }
        TBSReference.SAVE_BATCH(tag, registries, this.tinyBS);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(@NotNull Connection net, @NotNull ClientboundBlockEntityDataPacket pkt, HolderLookup.@NotNull Provider lookupProvider)
    {
        super.onDataPacket(net, pkt, lookupProvider);
        if (level != null)
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
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
    // region Getters
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

    public void setPlayerIsBreaking(@Nullable TinyBlockState playerIsBreaking) {
        this.playerIsBreaking = playerIsBreaking;
    }

    public @Nullable TinyBlockState getPlayerIsBreaking() {
        return playerIsBreaking;
    }

    /**
     * Get a TinyState where an entity is stepping.
     */
    public @Nullable TinyBlockState getTinyStateAboveEntity(@NotNull Entity entity)
    {
        var old = entity.getBoundingBox();
        AABB shape = new AABB(old.minX, old.minY, old.minZ, old.maxX, old.maxY, old.maxZ).move(Vec3.atLowerCornerOf(getBlockPos()).reverse());
        if (Shapes.block().bounds().intersects(shape))//check if the entity is inside the SubBlock
            for (var state : tinyBS)//for each state check if the state shape collides with the entity shape
                for (AABB aabb : state.getTranslatedAABB())
                    if (aabb.minX < shape.maxX && aabb.maxX > shape.minX && aabb.minY < shape.maxY && aabb.maxY >= shape.minY && aabb.minZ < shape.maxZ && aabb.maxZ > shape.minZ)
                        return state;

        return null;
    }
    //endregion
    //region Helpers
    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------Helpers/extras-------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Used to get the aiming TinyBlockState in a SubBloc,
     * This method uses the {@link Player#pick(double, float, boolean)} method to pick a block in the player range;
     * @param player The player to use to the RayCast.
     * @return Returns the aimed TinyBlockState or null if don't find one.
     */
    public @Nullable TinyBlockState getTinyStateByRayCast(Player player)
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

    /// A helper to set the breaking block based on ray cast
    /// @return if the ray cast success.
    boolean setBreakingByRayCast(@NotNull Player player)
    {
        TinyBlockState state = getTinyStateByRayCast(player);
        if (state != null)
            playerIsBreaking = state;

        return state != null;
    }

    /// Returns if a TinyBLock can be place at this position in the world.
    public static boolean CAN_PLACE(@NotNull TinyBlock block, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Vec3 pos, @NotNull Direction face)
    {
        Vec3i vec = Grid16Context.grid_from(level, blockPos, pos, face);

        //check if the clicked block is a tiny block
        if (level.getBlockEntity(blockPos) instanceof SubBlockBE subBlockBE)
        {
            return subBlockBE.canPlace(block, vec.getX(), vec.getY(), vec.getZ());
        }
        else
        {
            final BlockPos relative = blockPos.relative(face);
            //if isn't, check if the block facing the clicked direction is a sub block
            if (level.getBlockEntity(relative) instanceof SubBlockBE subBlockBE)
            {
                //if the block in the clicked direction is a sub block, then get the BE
                return subBlockBE.canPlace(block, vec.getX(), vec.getY(), vec.getZ());
            }
            else
            {
                VoxelShape translated = block.getTranslatedShape(new TinyBlockState(block, vec.getX(), vec.getY(), vec.getZ()));
                //check in the surrounds if was collision
                var translatedAABB = translated.toAabbs();

                for (Direction dir : Direction.values())
                {
                    BlockState state = level.getBlockState(relative.relative(dir));
                    if (state.isAir())
                        continue;

                    VoxelShape shape = state.getShape(level, relative.relative(dir));
                    List<AABB> aabbList = shape.toAabbs().stream().map(a -> a.move(dir.getStepX(), dir.getStepY(), dir.getStepZ())).toList();
                    for (AABB aabb : aabbList)
                        for (AABB aabb1 : translatedAABB)
                            if (aabb.intersects(aabb1))
                                return false;
                }

                //todo implement multiples-BlockEntity states
                //create the BE only if the placement is in one block
                var translatedBounds = translated.bounds();
                if (Shapes.block().bounds().intersects(translatedBounds))
                {
                    if (Shapes.block().bounds().intersect(translatedBounds).equals(translatedBounds))
                    {
                        return true;
                    } else
                    {
                        AmberCraft.LOGGER.warn("Non implemented branch!!!, TinyItem.CAN_PLACE:Multiples-BlockEntity");
                        return false;
                    }
                }
            }
        }

        return false;
    }
    //endregion
}
