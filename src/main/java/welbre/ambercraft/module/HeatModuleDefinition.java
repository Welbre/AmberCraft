package welbre.ambercraft.module;

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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class HeatModuleDefinition implements ModuleDefinition {

    @Override
    public void tick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (!level.isClientSide)
            if (blockEntity instanceof ModularBlockEntity modular)
                for (var m : modular.getModules())
                    m.tick(level, pos, state, blockEntity);
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide){
            if (stack.getItem() == Items.LEVER){
                BlockEntity entity = level.getBlockEntity(pos);
                if (entity instanceof ModularBlockEntity modular) {
                    HeatModule[] modules;
                    if ((modules = modular.getModule(HeatModule.class, null)) != null) {
                        for (HeatModule module : modules) {
                            ((ServerPlayer) player).sendSystemMessage(Component.literal(module.getTemperature() + "ºC"), false);
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        } else {
            if (stack.getItem() == Items.LEVER)
                return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide){
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof ModularBlockEntity modular) {
                HeatModule[] modules = modular.getModule(HeatModule.class, null);
                for (HeatModule module : modules) {
                    if (module.temperature > 100) {
                        entity.hurtServer((ServerLevel) level, level.damageSources().inFire(), (float) (module.getTemperature() / 100f));
                    }
                }
            }
        }
    }
}
