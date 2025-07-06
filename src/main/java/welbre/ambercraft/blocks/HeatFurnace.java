package welbre.ambercraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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
import welbre.ambercraft.blockentity.HeatFurnaceBE;
import welbre.ambercraft.blocks.parent.AmberHorizontalBlock;
import welbre.ambercraft.module.heat.HeatModuleFactory;

public class HeatFurnace extends AmberHorizontalBlock implements EntityBlock {
    public HeatModuleFactory factory = new HeatModuleFactory(module -> {
        module.alloc();
        module.getHeatNode().setThermalConductivity(100.0);
    });

    public HeatFurnace(Properties p) {
        super(p);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide){
            if (stack.getItem() == Items.LEVER){
                BlockEntity entity = level.getBlockEntity(pos);
                if (entity instanceof HeatFurnaceBE furnace) {
                    player.displayClientMessage(Component.literal(furnace.heatModule.getHeatNode().getTemperature() + "ºC").withColor(DyeColor.ORANGE.getTextColor()), false);
                    return InteractionResult.SUCCESS;
                }
            } else if (stack.getItem() == Items.COAL) {
                BlockEntity entity = level.getBlockEntity(pos);
                if (entity instanceof HeatFurnaceBE furnace) {
                    furnace.addBoost();
                    stack.consume(10, player);
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
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof HeatFurnaceBE furnace && !level.isClientSide)
            furnace.heatModule = factory.get();
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof HeatFurnaceBE furnace)
            furnace.heatModule.free();
        super.destroy(level, pos, state);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (level.getBlockEntity(pos) instanceof HeatFurnaceBE furnace)
            factory.getType().stepOn(furnace.heatModule,level,pos,state,entity);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HeatFurnaceBE(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == Main.BlockEntity.HEAT_FURNACE_BE.get() ? HeatFurnaceBE::tick : null;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof HeatFurnaceBE tile) {
            tile.setOverCharged(level.getBlockState(pos.below()).getBlock() == Blocks.LAVA);
        }
    }


}
