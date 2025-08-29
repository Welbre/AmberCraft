package welbre.ambercraft.blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.FacedCableBE;
import welbre.ambercraft.cables.*;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.network.facedcable.FacedCableRemoveFacePayload;
import welbre.ambercraft.network.facedcable.FacedCableStateChangePayload;

import java.util.Optional;

public class FacedCableBlock extends Block implements EntityBlock {
    /**
     * uses 5 bits (16 states) to represent all possibility in cable connection. NORTH(0), WEST(1), SOUTH(2), EAST(3) in big endian order.
     */
    public FacedCableBlock(Properties p) {
        super(p);
        p.noOcclusion().sound(SoundType.METAL).strength(1f).destroyTime(0.5f);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        return state;
    }

    /// server only
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        if (level.getBlockEntity(pos) instanceof FacedCableBE cable && level instanceof ServerLevel serverLevel)
        {
            boolean changed = false;
            //check anchors
            for (Direction face : cable.getState().getCenterDirections())
            {
                if (!level.getBlockState(pos.relative(face)).isFaceSturdy(level, pos, face.getOpposite()))//remove if the anchor is ar
                {
                    changed = true;
                    cable.dropCenter(face);
                    cable.removeCenter(face);
                }
            }
            if (cable.getState().isEmpty())//remove if is empty
            {
                level.removeBlock(pos, false);
                return;
            }

            final FacedCableBE.UpdateShapeResult result = cable.updateState();
            if (result.changed() || changed)
                PacketDistributor.sendToPlayersInDimension(serverLevel, new FacedCableStateChangePayload(cable));
        }
    }

    /// Server only, used in the case that non-player set the block in the world, like /setblock (x) (y) (z) ambercraft: abstract_faced_cable{...}.<br>
    /// The player cable placement is handled by {@link welbre.ambercraft.item.FacedCableBlockItem FacedCableBlockItem} duo the dependent where the player clicks.
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.getBlockEntity(pos) instanceof FacedCableBE cable && level instanceof ServerLevel serverLevel)
        {
            var result = cable.updateState();
            for (var blockPos : result.diagonal())
                serverLevel.neighborChanged(blockPos, this, null);
            PacketDistributor.sendToPlayersInDimension(serverLevel, new FacedCableStateChangePayload(cable));
            //The brain is updated in the FacedCableBE#onLoad()
        }
    }

    ///Server only, Is trigger only if the block it-self is removed, remove a face from the FacedCableBlockEntity don't call this.<br>
    ///Don't call this from other methods, is used by the Minecraft level.
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (level.getBlockEntity(pos) instanceof FacedCableBE faced && level.isClientSide)//neighbor removed
        {
            Optional<Direction> dir = GET_FACE_DIRECTION_USING_RAY_CAST(faced, pos, player);
            if (dir.isEmpty())
                return false;

            if (faced.getState().getFaceStatus(dir.get()) != null)
            {
                PacketDistributor.sendToServer(new FacedCableRemoveFacePayload(pos, dir.get()));
                return false;
            }
        }
        return false;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState bstate, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof FacedCableBE cable)
        {
            Optional<Direction> dir = GET_FACE_DIRECTION_USING_RAY_CAST(cable, pos, player);
            if (dir.isEmpty())
                return InteractionResult.FAIL;

            FaceBrain brain = cable.getBrain().getFaceBrain(dir.get());
            InteractionResult result = InteractionResult.TRY_WITH_EMPTY_HAND;
            if (brain != null)
            {
                for (Module module : brain.modules())
                {
                    var resultLocal = module.getType().useItemOn(module, stack, bstate, level, pos, player, hand, hitResult);
                    if (resultLocal.consumesAction())
                        result = resultLocal;
                }
                return result;
            }
        }
        return super.useItemOn(stack, bstate, level, pos, player, hand, hitResult);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof FacedCableBE cable)
        {
            if (player.isCrouching())
            {
                if (level instanceof ServerLevel serverLevel)
                {
                    Optional<Direction> direction = GET_FACE_DIRECTION_USING_RAY_CAST(cable, pos, player);
                    if (direction.isEmpty())
                        return InteractionResult.FAIL;

                    FaceState status = cable.getState().getFaceStatus(direction.get());
                    status.data.ignoreColor = !status.data.ignoreColor;

                    final var result = cable.updateState();
                    cable.updateBrain();

                    cable.setChanged();

                    if (result.changed())
                        PacketDistributor.sendToPlayersInDimension(serverLevel, new FacedCableStateChangePayload(cable));

                    for (var p : result.diagonal())
                        serverLevel.neighborChanged(p, this, null);
                    serverLevel.updateNeighborsAt(pos, this);

                    player.displayClientMessage(Component.literal("Cable toggled!").withColor(0x08dd08), false);
                }
                return InteractionResult.SUCCESS;
            }
            else
            {
                player.displayClientMessage(Component.literal(cable.getState() + " client: " + level.isClientSide).withColor(level.isClientSide ?
                        DyeColor.ORANGE.getTextColor() : DyeColor.LIGHT_BLUE.getTextColor()), false);
                player.displayClientMessage(Component.empty(), false);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }



    @Override
    public @NotNull ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        if (Minecraft.getInstance().hitResult.getType() == HitResult.Type.BLOCK){
            BlockHitResult block = (BlockHitResult) Minecraft.getInstance().hitResult;
            if (level.getBlockEntity(block.getBlockPos()) instanceof FacedCableBE cable)
            {
                Optional<Direction> face = GET_FACE_DIRECTION_USING_RAY_CAST(cable, pos, player);
                if (face.isEmpty())
                    return ItemStack.EMPTY;

                FaceState faceState = cable.getState().getFaceStatus(face.get());
                var stack = new ItemStack(AmberCraft.Items.FACED_CABLE_BLOCK_ITEM.get());
                var data = new CableData(faceState.data);
                data.ignoreColor = false;
                var comp = new FacedCableComponent(faceState.type.cable_type_index, data);

                stack.set(AmberCraft.Components.CABLE_DATA_COMPONENT.get(), comp);
                return stack;
            }
        }
        return super.getCloneItemStack(level, pos, state, includeData, player);
    }

    public static Optional<Direction> GET_FACE_DIRECTION_USING_RAY_CAST(FacedCableBE cable, BlockPos pos, Entity entity)
    {
        Vec3 start = entity.getEyePosition(0);
        Vec3 temp = entity.getViewVector(0);
        Vec3 end = start.add(temp.x * 5, temp.y * 5, temp.z * 5);

        CableState center = cable.getState();
        if (center.getFaceStatus(Direction.DOWN) != null)
            if (Shapes.box(0, 0, 0, 1, .2, 1).clip(start,end,pos) != null)
                return Optional.of(Direction.DOWN);
        if (center.getFaceStatus(Direction.UP) != null)
            if (Shapes.box(0, 0.8, 0, 1, 1, 1).clip(start,end,pos) != null)
                return Optional.of(Direction.UP);
        if (center.getFaceStatus(Direction.NORTH) != null)
            if (Shapes.box(0, 0, 0, 1, 1, .2).clip(start,end,pos) != null)
                return Optional.of(Direction.NORTH);
        if (center.getFaceStatus(Direction.SOUTH) != null)
            if (Shapes.box(0, 0, 0.8, 1, 1, 1).clip(start,end,pos) != null)
                return Optional.of(Direction.SOUTH);
        if (center.getFaceStatus(Direction.WEST) != null)
            if (Shapes.box(0, 0, 0, .2, 1, 1).clip(start,end,pos) != null)
                return Optional.of(Direction.WEST);
        if (center.getFaceStatus(Direction.EAST) != null)
            if (Shapes.box(0.8, 0, 0, 1, 1, 1).clip(start,end,pos) != null)
                return Optional.of(Direction.EAST);
        return Optional.empty();
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FacedCableBE(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return ModulesHolder::TICK_HELPER;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = Shapes.empty();
        if (level.getBlockEntity(pos) instanceof FacedCableBE faced)
        {
            CableState center = faced.getState();
            if (center.getFaceStatus(Direction.DOWN) != null)
                shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, .25/2f, 1), BooleanOp.OR);
            if (center.getFaceStatus(Direction.UP) != null)
                shape = Shapes.join(shape, Shapes.box(0, 1-0.25/2f, 0, 1, 1, 1), BooleanOp.OR);
            if (center.getFaceStatus(Direction.NORTH) != null)
                shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 1, .25/2f), BooleanOp.OR);
            if (center.getFaceStatus(Direction.SOUTH) != null)
                shape = Shapes.join(shape, Shapes.box(0, 0, 1-0.25/2f, 1, 1, 1), BooleanOp.OR);
            if (center.getFaceStatus(Direction.WEST) != null)
                shape = Shapes.join(shape, Shapes.box(0, 0, 0, .25/2f, 1, 1), BooleanOp.OR);
            if (center.getFaceStatus(Direction.EAST) != null)
                shape = Shapes.join(shape, Shapes.box(1-0.25/2f, 0, 0, 1, 1, 1), BooleanOp.OR);
        }
        return shape;
    }
}
