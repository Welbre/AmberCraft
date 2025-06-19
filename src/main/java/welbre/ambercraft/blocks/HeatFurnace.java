package welbre.ambercraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.Main;
import welbre.ambercraft.blockentity.HeatFurnaceTile;
import welbre.ambercraft.blocks.parent.AmberHorizontalBlock;

public class HeatFurnace extends AmberHorizontalBlock implements EntityBlock {
    public HeatFurnace(Properties p) {
        super(p);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide){
            if (stack.getItem() == Items.LEVER){
                BlockEntity entity = level.getBlockEntity(pos);
                if (entity instanceof HeatFurnaceTile furnace) {
                    ((ServerPlayer) player).sendSystemMessage(Component.literal(furnace.heatModule.getTemperature() + "ºC"), false);
                    return InteractionResult.SUCCESS;
                }
            } else if (stack.getItem() == Items.COAL) {
                BlockEntity entity = level.getBlockEntity(pos);
                if (entity instanceof HeatFurnaceTile furnace) {
                    furnace.addBoost();
                    stack.consume(1, player);
                    return InteractionResult.SUCCESS;
                }
            }
        } else {
            if (stack.getItem() == Items.LEVER)
                return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack,state,level,pos,player,hand, hitResult);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HeatFurnaceTile(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == Main.Tiles.HEAT_FURNACE_TILE.get() ? HeatFurnaceTile::tick : null;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof HeatFurnaceTile tile) {
            tile.setOverCharged(level.getBlockState(pos.below()).getBlock() == Blocks.LAVA);
        }
    }


}
