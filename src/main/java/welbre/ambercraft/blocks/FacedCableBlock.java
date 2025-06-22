package welbre.ambercraft.blocks;

import com.mojang.math.Transformation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.apache.logging.log4j.core.util.Transform;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.blockentity.FacedCableBlockEntity;

import java.util.ArrayList;
import java.util.List;

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
            int center = faced.getConnection_mask() & 0b10000_10000_10000_10000_10000_10000;
            if ((center & (1 << 4)) != 0)
                shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, .2, 1), BooleanOp.OR);
            if ((center & (1 << 4+5)) != 0)
                shape = Shapes.join(shape, Shapes.box(0, 0.8, 0, 1, 1, 1), BooleanOp.OR);
            if ((center & (1 << 4+10)) != 0)
                shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 1, .2), BooleanOp.OR);
            if ((center & (1 << 4+15)) != 0)
                shape = Shapes.join(shape, Shapes.box(0, 0, 0.8, 1, 1, 1), BooleanOp.OR);
            if ((center & (1 << 4+20)) != 0)
                shape = Shapes.join(shape, Shapes.box(0, 0, 0, .2, 1, 1), BooleanOp.OR);
            if ((center & (1 << 4+25)) != 0)
                shape = Shapes.join(shape, Shapes.box(0.8, 0, 0, 1, 1, 1), BooleanOp.OR);
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
            player.displayClientMessage(Component.literal(faced.getConnection_mask()+ " client: " + level.isClientSide),false);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }
}
