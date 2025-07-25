package welbre.ambercraft.module.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.HeatConductorBE;
import welbre.ambercraft.debug.NetworkWrapperModule;
import welbre.ambercraft.module.ModuleType;
import welbre.ambercraft.network.NetworkViewerPayLoad;

public class HeatModuleType implements ModuleType<HeatModule> {

    @Override
    public HeatModule createModule() {
        return new HeatModule();
    }

    @Override
    public void neighborChanged(HeatModule module, BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {

    }

    @Override
    public void onNeighborChange(HeatModule module, BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor)
    {

    }

    @Override
    public InteractionResult useWithoutItem(HeatModule module, BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return null;
    }

    @Override
    public InteractionResult useItemOn(HeatModule module, ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide){
            if (stack.getItem() == Items.LEVER){
                player.displayClientMessage(Component.literal(module.getHeatNode().getTemperature() + "ºC").withColor(DyeColor.ORANGE.getTextColor()), false);
                return InteractionResult.SUCCESS;
            }
            else if (stack.getItem() == AmberCraft.Items.MULTIMETER.get()) {
                PacketDistributor.sendToPlayer((ServerPlayer) player, new NetworkViewerPayLoad((HeatConductorBE) level.getBlockEntity(pos)));
                return InteractionResult.SUCCESS;
            }
        } else {
            if (stack.getItem() == Items.LEVER || stack.getItem() == AmberCraft.Items.MULTIMETER.get())
                return InteractionResult.SUCCESS;
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    public void stepOn(HeatModule module, Level level, BlockPos pos, BlockState state, Entity entity) {
        if (level.isClientSide())
            return;
        if (module.getHeatNode().getTemperature() > 100)
            entity.hurtServer((ServerLevel) level, level.damageSources().inFire(), (float) (module.getHeatNode().getTemperature() / 100f));
    }

}
