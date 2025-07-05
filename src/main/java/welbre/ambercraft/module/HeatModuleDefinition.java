package welbre.ambercraft.module;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class HeatModuleDefinition implements ModuleDefinition<HeatModule, BlockEntity> {

    @Override
    public InteractionResult useWithoutItem(HeatModule module, BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return null;
    }

    @Override
    public HeatModule createModule(BlockEntity obj) {
        return new HeatModule(obj);
    }

    @Override
    public InteractionResult useItemOn(HeatModule module, ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide){
            if (stack.getItem() == Items.LEVER){
                player.displayClientMessage(Component.literal(module.getHeatNode().getTemperature() + "ºC").withColor(DyeColor.ORANGE.getTextColor()), false);
                return InteractionResult.SUCCESS;
            }
        } else {
            if (stack.getItem() == Items.LEVER)
                return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    public void stepOn(HeatModule module, Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide)
            return;
        if (module.getHeatNode().getTemperature() > 100)
            entity.hurtServer((ServerLevel) level, level.damageSources().inFire(), (float) (module.getHeatNode().getTemperature() / 100f));
    }
}
