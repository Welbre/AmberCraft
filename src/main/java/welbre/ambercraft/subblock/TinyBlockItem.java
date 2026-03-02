package welbre.ambercraft.subblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;

import java.util.List;

/**
 * A class with methods to help any item to beable to place TinyBlock in the world.
 */
public class TinyBlockItem extends Item
{

    public TinyBlockItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context)
    {
        ItemStack stack = context.getItemInHand();
        TinyItemDataComponent component = stack.get(AmberCraft.DataComponents.TINY_BLOCK_DATA_COMPONENT);
        if (component == null)
        {
            if (context.getPlayer() != null)
                //if the stack hasn't a tiny block component, the stack is in a broken state, so remove it from the player.
                context.getPlayer().setItemInHand(context.getHand(), new ItemStack(Items.AIR));
            return InteractionResult.FAIL;
        }

        TinyBlock tinyBlock = component.get();
        Level level = context.getLevel();
        var pos = CONTEXT_TO_16_GRID(context);
        SubBlockBE sub = GET_SUB_BLOCK_BE_IF_CAN_PLACE(tinyBlock, level, context);

        if (sub == null) {
            return InteractionResult.FAIL;
        }

        //Place Tiny Item
        if (!sub.addTinyBlock(tinyBlock, pos.getX(), pos.getY(), pos.getZ()))
            return InteractionResult.FAIL;

        //Play sound
        SoundType soundtype = tinyBlock.getSoundType(sub.tinyBS.getLast());

        level.playSound(
                context.getPlayer(),
                sub.getBlockPos(),
                soundtype.getPlaceSound(),
                SoundSource.BLOCKS,
                (soundtype.getVolume() + 1.0F) / 2.0F,
                soundtype.getPitch() * 0.8F
        );

        //consume amount
        stack.shrink(1);

        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        TinyItemDataComponent component = stack.get(AmberCraft.DataComponents.TINY_BLOCK_DATA_COMPONENT);
        if (component == null)
            return Component.literal("invalid tiny block");
        else
            return component.get().getTinyItemName();
    }



    //------------------------------------------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------------Helpers--------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------------------------------------



    public static Vec3i CONTEXT_TO_16_GRID(UseOnContext context)
    {
        //value between 0 and 1
        Vec3 r;

        if (context.getLevel().getBlockState(context.getClickedPos()).is(AmberCraft.Blocks.SUB_BLOCK.get()))
            r = context.getClickLocation().subtract(new Vec3(context.getClickedPos().getX(), context.getClickedPos().getY(), context.getClickedPos().getZ()));
        else
            r = context.getClickLocation().subtract(new Vec3(context.getClickedPos().getX(), context.getClickedPos().getY(), context.getClickedPos().getZ()).add(context.getClickedFace().getUnitVec3()));

        final int x,y,z;
        //if some value in r == 1, then the grid algorithm will return 0 at that coordinate, so multiply by 0.999f to 0.9999f * 16 != 16
        if (context.getClickedFace().getUnitVec3().x < 0 || context.getClickedFace().getUnitVec3().y < 0 || context.getClickedFace().getUnitVec3().z < 0)
            r = r.multiply(0.999f, 0.999f, 0.999f);

        x = (int) (r.x * 16) % 16; y = (int) (r.y * 16) % 16; z = (int) (r.z * 16) % 16;
        return new Vec3i(x,y,z);
    }

    public static Vec3i CONTEXT_TO_16_GRID(Level level, BlockHitResult result)
    {
        //value between 0 and 1
        Vec3 r;

        if (level.getBlockState(result.getBlockPos()).is(AmberCraft.Blocks.SUB_BLOCK.get()))
            r = result.getLocation().subtract(new Vec3(result.getBlockPos().getX(), result.getBlockPos().getY(), result.getBlockPos().getZ()));
        else
            r = result.getLocation().subtract(new Vec3(result.getBlockPos().getX(), result.getBlockPos().getY(), result.getBlockPos().getZ()).add(result.getDirection().getUnitVec3()));

        final int x,y,z;
        //if some value in r == 1, then the grid algorithm will return 0 at that coordinate, so multiply by 0.999f to 0.9999f * 16 != 16
        if (result.getDirection().getUnitVec3().x < 0 || result.getDirection().getUnitVec3().y < 0 || result.getDirection().getUnitVec3().z < 0)
            r = r.multiply(0.999f, 0.999f, 0.999f);

        x = (int) (r.x * 16) % 16; y = (int) (r.y * 16) % 16; z = (int) (r.z * 16) % 16;
        return new Vec3i(x,y,z);
    }

    /**
     * Used to check if a TinyBlock can be placed in this position in the world.<br>
     * @return Null if can't place, otherwise, the subblock.
     */
    public static @Nullable SubBlockBE GET_SUB_BLOCK_BE_IF_CAN_PLACE(@NotNull TinyBlock block, @NotNull Level level, @NotNull UseOnContext context)
    {
        Vec3i vec = CONTEXT_TO_16_GRID(context);
        BlockPos pos = context.getClickedPos();

        //check if the clicked block is a tiny block
        if (level.getBlockEntity(pos) instanceof SubBlockBE subBlockBE)
        {
            if (subBlockBE.canPlace(block, vec.getX(), vec.getY(), vec.getZ()))
                return subBlockBE;
        }
        else
        {
            final BlockPos relative = pos.relative(context.getClickedFace());
            //if isn't, check if the block facing the clicked direction is a sub block
            if (level.getBlockEntity(relative) instanceof SubBlockBE subBlockBE)
            {
                //if the block in the clicked direction is a sub block, then get the BE
                if (subBlockBE.canPlace(block, vec.getX(), vec.getY(), vec.getZ()))
                    return subBlockBE;
            }
            else
            {
                //check in the surrounds if was collision
                var translatedAABB = block.getTranslatedAABB(new TinyBlockState(block, vec.getX(), vec.getY(), vec.getZ()));

                for (Direction face : Direction.values())
                {
                    BlockState state = level.getBlockState(relative.relative(face));
                    if (state.isAir())
                        continue;

                    VoxelShape shape = state.getShape(level, relative.relative(face));
                    List<AABB> aabbList = shape.toAabbs().stream().map(a -> a.move(face.getStepX(), face.getStepY(), face.getStepZ())).toList();
                    for (AABB aabb : aabbList)
                        for (AABB aabb1 : translatedAABB)
                            if (aabb.intersects(aabb1))
                                return null;
                }

                //todo implement multiples-BlockEntity states
                //create the BE only if the placement is in one block
                var translatedBounds = block.getTranslatedBounds(new TinyBlockState(block, vec.getX(), vec.getY(), vec.getZ()));
                if (Shapes.block().bounds().intersects(translatedBounds))
                    if (Shapes.block().bounds().intersect(translatedBounds).equals(translatedBounds))
                    {
                        level.setBlockAndUpdate(relative, AmberCraft.Blocks.SUB_BLOCK.get().defaultBlockState());
                        return (SubBlockBE) level.getBlockEntity(relative);
                    }
                    else
                    {
                        AmberCraft.LOGGER.warn("Non implemented branch!!!, TinyItem.GET_SUB_BLOCK_BE_IF_CAN_PLACE:Multiples-BlockEntity");
                        return null;
                    }
            }
        }

        return null;
    }

    public static boolean CAN_PLACE(@NotNull TinyBlock block, @NotNull Level level, @NotNull BlockHitResult context)
    {
        Vec3i vec = CONTEXT_TO_16_GRID(level, context);
        BlockPos pos = context.getBlockPos();

        //check if the clicked block is a tiny block
        if (level.getBlockEntity(pos) instanceof SubBlockBE subBlockBE)
        {
            return subBlockBE.canPlace(block, vec.getX(), vec.getY(), vec.getZ());
        }
        else
        {
            final BlockPos relative = pos.relative(context.getDirection());
            //if isn't, check if the block facing the clicked direction is a sub block
            if (level.getBlockEntity(relative) instanceof SubBlockBE subBlockBE)
            {
                //if the block in the clicked direction is a sub block, then get the BE
                return subBlockBE.canPlace(block, vec.getX(), vec.getY(), vec.getZ());
            }
            else
            {
                //check in the surrounds if was collision
                var translatedAABB = block.getTranslatedAABB(new TinyBlockState(block, vec.getX(), vec.getY(), vec.getZ()));

                for (Direction face : Direction.values())
                {
                    BlockState state = level.getBlockState(relative.relative(face));
                    if (state.isAir())
                        continue;

                    VoxelShape shape = state.getShape(level, relative.relative(face));
                    List<AABB> aabbList = shape.toAabbs().stream().map(a -> a.move(face.getStepX(), face.getStepY(), face.getStepZ())).toList();
                    for (AABB aabb : aabbList)
                        for (AABB aabb1 : translatedAABB)
                            if (aabb.intersects(aabb1))
                                return false;
                }

                //todo implement multiples-BlockEntity states
                //create the BE only if the placement is in one block
                var translatedBounds = block.getTranslatedBounds(new TinyBlockState(block, vec.getX(), vec.getY(), vec.getZ()));
                if (Shapes.block().bounds().intersects(translatedBounds))
                    if (Shapes.block().bounds().intersect(translatedBounds).equals(translatedBounds))
                    {
                        level.setBlockAndUpdate(relative, AmberCraft.Blocks.SUB_BLOCK.get().defaultBlockState());
                        return true;
                    }
                    else
                    {
                        AmberCraft.LOGGER.warn("Non implemented branch!!!, TinyItem.CAN_PLACE:Multiples-BlockEntity");
                        return false;
                    }
            }
        }

        return false;
    }
}
