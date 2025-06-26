package welbre.ambercraft.blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.Main;
import welbre.ambercraft.blockentity.FacedCableBlockEntity;
import welbre.ambercraft.cables.CableDataComponent;
import welbre.ambercraft.cables.CableStatus;
import welbre.ambercraft.cables.FaceStatus;

public class FacedCableBlock extends Block implements EntityBlock {
    /**
     * uses 5 bits (16 states) to represent all possibility in cable connection. NORTH(0), WEST(1), SOUTH(2), EAST(3) in big endian order.
     */
    public FacedCableBlock(Properties p) {
        super(p);
        p.noOcclusion().sound(SoundType.METAL).strength(1f);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FacedCableBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = Shapes.empty();
        if (level.getBlockEntity(pos) instanceof FacedCableBlockEntity faced)
        {
            CableStatus center = faced.getStatus();
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

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        super.destroy(level, pos, state);

    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.getBlockEntity(pos) instanceof FacedCableBlockEntity faced)//neighbor removed
        {
            faced.removeCable(level, pos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof FacedCableBlockEntity faced)
        {
            if (player.isCrouching())
            {
                FaceStatus status = GET_FACE_STATUS_USING_RAY_CAST(faced, pos, player);
                status.type = (byte) ((status.type == -1) ? 0 : -1);
                faced.calculateState(level, pos);
                level.markAndNotifyBlock(pos,level.getChunkAt(pos),level.getBlockState(pos),level.getBlockState(pos),3,512);
                if (level.isClientSide)
                    player.displayClientMessage(Component.literal("Cable toggled!").withColor(0x08dd08), false);
            }
            else
            {
                player.displayClientMessage(Component.literal(faced.getStatus() + " client: " + level.isClientSide), false);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }



    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        if (Minecraft.getInstance().hitResult.getType() == HitResult.Type.BLOCK){
            BlockHitResult block = (BlockHitResult) Minecraft.getInstance().hitResult;
            if (level.getBlockEntity(block.getBlockPos()) instanceof FacedCableBlockEntity faced){
                return GET_ITEM_STACK_FROM_FACE_STATUS(GET_FACE_STATUS_USING_RAY_CAST(faced, pos, player));
            }
        }
        System.out.println();
        return super.getCloneItemStack(level, pos, state, includeData, player);
    }

    public static @NotNull FaceStatus GET_FACE_STATUS_USING_RAY_CAST(FacedCableBlockEntity cable, BlockPos pos, Entity entity){
        Vec3 start = entity.getEyePosition(0);
        Vec3 temp = entity.getViewVector(0);
        Vec3 end = start.add(temp.x * 5, temp.y * 5, temp.z * 5);

        CableStatus center = cable.getStatus();
        if (center.getFaceStatus(Direction.DOWN) != null)
            if (Shapes.box(0, 0, 0, 1, .2, 1).clip(start,end,pos) != null)
                return center.getFaceStatus(Direction.DOWN);
        if (center.getFaceStatus(Direction.UP) != null)
            if (Shapes.box(0, 0.8, 0, 1, 1, 1).clip(start,end,pos) != null)
                return center.getFaceStatus(Direction.UP);
        if (center.getFaceStatus(Direction.NORTH) != null)
            if (Shapes.box(0, 0, 0, 1, 1, .2).clip(start,end,pos) != null)
                return center.getFaceStatus(Direction.NORTH);
        if (center.getFaceStatus(Direction.SOUTH) != null)
            if (Shapes.box(0, 0, 0.8, 1, 1, 1).clip(start,end,pos) != null)
                return center.getFaceStatus(Direction.SOUTH);
        if (center.getFaceStatus(Direction.WEST) != null)
            if (Shapes.box(0, 0, 0, .2, 1, 1).clip(start,end,pos) != null)
                return center.getFaceStatus(Direction.WEST);
        if (center.getFaceStatus(Direction.EAST) != null)
            if (Shapes.box(0.8, 0, 0, 1, 1, 1).clip(start,end,pos) != null)
                return center.getFaceStatus(Direction.EAST);
        throw new IllegalStateException("Faced not Raycasted!");
    }

    private static ItemStack GET_ITEM_STACK_FROM_FACE_STATUS(FaceStatus faceStatus){
        var stack = new ItemStack(Main.Items.FACED_CABLE_BLOCK_ITEM.get());
        stack.set(Main.Components.CABLE_DATA_COMPONENT.get(), new CableDataComponent(faceStatus.color, faceStatus.type, faceStatus.packed_size));
        return stack;
    }
}
