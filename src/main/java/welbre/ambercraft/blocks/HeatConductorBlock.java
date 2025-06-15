package welbre.ambercraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.blockentity.HeatBlockEntity;
import welbre.ambercraft.blockentity.HeatConductorTile;
import welbre.ambercraft.blocks.parent.AmberBasicBlock;

public abstract class HeatConductorBlock extends AmberBasicBlock implements EntityBlock {
    private static final BlockEntityTicker<BlockEntity> TICKER = (level, pos, state, blockEntity) -> {
        if (!level.isClientSide){
            if (blockEntity instanceof HeatBlockEntity heat)
                heat.transferHeatToNeighbor(level, pos);
        }
    };

    public HeatConductorBlock(Properties p) {
        super(p);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide){
            if (stack.getItem() == Items.LEVER){
                BlockEntity entity = level.getBlockEntity(pos);
                if (entity instanceof HeatConductorTile conductor) {
                    ((ServerPlayer) player).sendSystemMessage(Component.literal(conductor.getTemperature() + "ºC"), false);
                    return InteractionResult.SUCCESS;
                }
            }
        } else {
            if (stack.getItem() == Items.LEVER)
                return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack,state,level,pos,player,hand, hitResult);
    }

    protected abstract BlockEntityType<? extends BlockEntity> getBlockEntityType();

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == getBlockEntityType() ? (BlockEntityTicker<T>) TICKER : null;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (!level.isClientSide){
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof HeatBlockEntity conductor) {
                if (conductor.getTemperature() > 100) {
                    entity.hurtServer((ServerLevel) level, level.damageSources().inFire(), (float) (conductor.getTemperature() / 100f));
                }
            }
        }
    }
}
