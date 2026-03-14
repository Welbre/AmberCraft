package welbre.ambercraft.subblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/// A helper to deal with placement of the TinyBlockState
public record Grid16Context(int x, int y, int z, BlockPos anchor, List<BlockPos> shared) {

    /// Copy constructor
    public Grid16Context(Grid16Context context) {
        this(context.x, context.y, context.z, context.anchor, context.shared);
    }

    /**
     * Converts a generic world position in a 16*16 grid
     *
     * @param block  The TinyBlock that will be placed.
     * @param anchor The block used as reference to the grid.
     * @param pos    The position to be converted to 16*16 grid.
     * @param face   The face of the block where the placement will happen; Can be understood as the plane that the grid is on.
     */
    public Grid16Context(TinyBlock block, Level level, BlockPos anchor, Vec3 pos, Direction face) {
        this(from(block, level, anchor, pos, face));
    }

    @SuppressWarnings("unused")
    public Vec3i toVec3i() {
        return new Vec3i(x, y, z);
    }

    public boolean hasShared() {
        return !shared.isEmpty();
    }

    /// Converts a generic world position in a 16*16 grid using a UseOnContext
    @SuppressWarnings("unused")
    public static Vec3i grid_from(UseOnContext context) {
        return grid_from(context.getLevel(), context.getClickedPos(), context.getClickLocation(), context.getClickedFace());
    }

    /// Converts a generic world position in a 16*16 grid using a BlockHitResult
    public static Vec3i grid_from(Level level, BlockHitResult result) {
        return grid_from(level, result.getBlockPos(), result.getLocation(), result.getDirection());
    }

    /**
     * Converts a generic world position in a 16*16 grid
     *
     * @param anchor The block used as reference to the grid
     * @param pos    The position to be converted to 16*16 grid
     * @param face   The face of the block where the placement will happen
     */
    public static Vec3i grid_from(Level level, BlockPos anchor, Vec3 pos, Direction face) {
        //value between 0 and 1
        Vec3 r;

        if (level.getBlockState(anchor).is(AmberCraft.Blocks.SUB_BLOCK.get()))
            r = pos.subtract(new Vec3(anchor.getX(), anchor.getY(), anchor.getZ()));//the pos is internal
        else
            r = pos.subtract(new Vec3(anchor.getX(), anchor.getY(), anchor.getZ()).add(face.getUnitVec3()));//the pos is outside the block

        final int x, y, z;
        //if some value in r == 1, then the grid algorithm will return 0 at that coordinate, so multiply by 0.999f to 0.9999f * 16 != 16
        if (face.getUnitVec3().x < 0 || face.getUnitVec3().y < 0 || face.getUnitVec3().z < 0)
            r = r.multiply(0.999f, 0.999f, 0.999f);

        x = (int) (r.x * 16) % 16;
        y = (int) (r.y * 16) % 16;
        z = (int) (r.z * 16) % 16;
        return new Vec3i(x, y, z);//value between [0,15]
    }


    private static @NotNull Grid16Context from(TinyBlock block, Level level, BlockPos anchor, Vec3 pos, Direction face) {
        Vec3i from = grid_from(level, anchor, pos, face);

        if (level.getBlockState(anchor).is(AmberCraft.Blocks.SUB_BLOCK.get()))
            return new Grid16Context(from.getX(), from.getY(), from.getZ(), anchor, GET_SHARED_LIST(block, anchor, from));//the pos is internal
        else
            return new Grid16Context(from.getX(), from.getY(), from.getZ(), anchor.relative(face), GET_SHARED_LIST(block, anchor.relative(face), from));//the pos is outside the block
    }

    /**
     * Get a list of all BlockPos outside the anchor bound
     * @param block The TinyBlock to test outside
     * @param anchor The position of the tested block
     * @param grid A grid with values between [0,15], use {@link Grid16Context#grid_from(Level, BlockPos, Vec3, Direction)}
     */
    public static @NotNull List<BlockPos> GET_SHARED_LIST(TinyBlock block, BlockPos anchor, Vec3i grid)
    {
        AABB bound = block.shape.bounds().move(grid.getX() / 16f, grid.getY() / 16f, grid.getZ() / 16f);
        List<BlockPos> shared = new ArrayList<>();

        //check if some edge is outside the bounds
        for (double x : new double[]{bound.minX, bound.maxX})
            if (x > 1 | x < 0) //if X plane is outside, don't need to check others
                for (double y : new double[]{bound.minY, bound.maxY})
                    for (double z : new double[]{bound.minZ, bound.maxZ})
                        CONVERT_AND_ADD(x, y, z, anchor, shared);
            else
                for (double y : new double[]{bound.minY, bound.maxY})
                    if (y > 1 | y < 0)//X and Y plane is outside the anchor block
                        for (double z : new double[]{bound.minZ, bound.maxZ})
                            CONVERT_AND_ADD(x, y, z, anchor, shared);
                    else
                        for (double z : new double[]{bound.minZ, bound.maxZ})//X, Y and Z plane is outside the anchor block
                            if (z > 1 | z < 0)
                                CONVERT_AND_ADD(x, y, z, anchor, shared);

        return shared;
    }

    private static void CONVERT_AND_ADD(double x, double y, double z, BlockPos anchor, Collection<BlockPos> shared) {
        BlockPos pos = new BlockPos((int) Math.round(anchor.getX() + x), (int) Math.round(anchor.getY() + y), (int) Math.round(anchor.getZ() + z));
        if (anchor.equals(pos))
            throw new RuntimeException("Anchor and position are the same");

        if (!shared.contains(pos))
            shared.add(pos);
    }

}