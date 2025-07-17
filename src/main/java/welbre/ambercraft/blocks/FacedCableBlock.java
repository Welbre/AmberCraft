package welbre.ambercraft.blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.FacedCableBE;
import welbre.ambercraft.cables.*;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.heat.HeatModule;

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
        return new FacedCableBE(pos, state);
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

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        super.destroy(level, pos, state);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (level.getBlockEntity(pos) instanceof FacedCableBE faced)//neighbor removed
        {
            Direction dir = GET_FACE_DIRECTION_USING_RAY_CAST(faced, pos, player);
            faced.removeCable(level, pos, dir);
            if (faced.getState().isEmpty())//remove the cable if is empty
                if (level.isClientSide)
                    return level.setBlock(pos, fluid.createLegacyBlock(), 11);
                else
                    return level.removeBlock(pos, false);
            else
            {
                level.markAndNotifyBlock(pos,level.getChunkAt(pos),level.getBlockState(pos),level.getBlockState(pos),3,512);
                return false;
            }
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState bstate, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() == Items.LEVER)
        {
            if (!level.isClientSide)
                if (level.getBlockEntity(pos) instanceof FacedCableBE cable)
                {
                    Direction dir = GET_FACE_DIRECTION_USING_RAY_CAST(cable, pos, player);
                    FaceBrain brain = cable.getBrain().getFaceBrain(dir);
                    if (brain != null)
                        for (Module module : brain.getModules())
                            if (module instanceof HeatModule heat)
                                player.displayClientMessage(Component.literal("Temperature %.2fºC".formatted(heat.getHeatNode().getTemperature())).withColor(DyeColor.ORANGE.getTextColor()), false);
                }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, bstate, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof FacedCableBE faced)
        {
            if (player.isCrouching())
            {
                FaceState status = GET_FACE_STATUS_USING_RAY_CAST(faced, pos, player);
                status.data.ignoreColor = !status.data.ignoreColor;
                faced.setChanged();
                faced.calculateState(level, pos);
                level.markAndNotifyBlock(pos,level.getChunkAt(pos),level.getBlockState(pos),level.getBlockState(pos),3,512);
                if (level.isClientSide)
                    player.displayClientMessage(Component.literal("Cable toggled!").withColor(0x08dd08), false);
            }
            else
            {
                player.displayClientMessage(Component.literal(faced.getState() + " client: " + level.isClientSide), false);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }



    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        if (Minecraft.getInstance().hitResult.getType() == HitResult.Type.BLOCK){
            BlockHitResult block = (BlockHitResult) Minecraft.getInstance().hitResult;
            if (level.getBlockEntity(block.getBlockPos()) instanceof FacedCableBE faced){
                return GET_ITEM_STACK_FROM_FACE_STATUS(GET_FACE_STATUS_USING_RAY_CAST(faced, pos, player));
            }
        }
        System.out.println();
        return super.getCloneItemStack(level, pos, state, includeData, player);
    }

    public static @NotNull FaceState GET_FACE_STATUS_USING_RAY_CAST(FacedCableBE cable, BlockPos pos, Entity entity){
        return cable.getState().getFaceStatus(GET_FACE_DIRECTION_USING_RAY_CAST(cable, pos, entity));
    }

    public static Direction GET_FACE_DIRECTION_USING_RAY_CAST(FacedCableBE cable, BlockPos pos, Entity entity)
    {
        Vec3 start = entity.getEyePosition(0);
        Vec3 temp = entity.getViewVector(0);
        Vec3 end = start.add(temp.x * 5, temp.y * 5, temp.z * 5);

        CableState center = cable.getState();
        if (center.getFaceStatus(Direction.DOWN) != null)
            if (Shapes.box(0, 0, 0, 1, .2, 1).clip(start,end,pos) != null)
                return Direction.DOWN;
        if (center.getFaceStatus(Direction.UP) != null)
            if (Shapes.box(0, 0.8, 0, 1, 1, 1).clip(start,end,pos) != null)
                return Direction.UP;
        if (center.getFaceStatus(Direction.NORTH) != null)
            if (Shapes.box(0, 0, 0, 1, 1, .2).clip(start,end,pos) != null)
                return Direction.NORTH;
        if (center.getFaceStatus(Direction.SOUTH) != null)
            if (Shapes.box(0, 0, 0.8, 1, 1, 1).clip(start,end,pos) != null)
                return Direction.SOUTH;
        if (center.getFaceStatus(Direction.WEST) != null)
            if (Shapes.box(0, 0, 0, .2, 1, 1).clip(start,end,pos) != null)
                return Direction.WEST;
        if (center.getFaceStatus(Direction.EAST) != null)
            if (Shapes.box(0.8, 0, 0, 1, 1, 1).clip(start,end,pos) != null)
                return Direction.EAST;
        throw new IllegalStateException("Faced not Raycasted!");
    }

    private static ItemStack GET_ITEM_STACK_FROM_FACE_STATUS(FaceState faceState){
        var stack = new ItemStack(AmberCraft.Items.FACED_CABLE_BLOCK_ITEM.get());
        var data = new CableData(faceState.data);
        data.ignoreColor = false;
        var comp = new AmberFCableComponent(faceState.type.cable_type_index, data);

        stack.set(AmberCraft.Components.CABLE_DATA_COMPONENT.get(), comp);
        return stack;
    }
}
