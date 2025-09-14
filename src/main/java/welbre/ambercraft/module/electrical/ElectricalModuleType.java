package welbre.ambercraft.module.electrical;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.ModuleType;

public class ElectricalModuleType implements ModuleType<ElectricalElementModule> {
    @Override
    public InteractionResult useWithoutItem(ElectricalElementModule module, BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useItemOn(ElectricalElementModule module, ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() == AmberCraft.Items.MULTIMETER.get())
        {
            if (!level.isClientSide())
                return AmberCraft.Items.MULTIMETER.get().handle(stack, (ServerPlayer) player, module);

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void stepOn(ElectricalElementModule module, Level level, BlockPos pos, BlockState state, Entity entity) {

    }

    @Override
    public void neighborChanged(ElectricalElementModule module, BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {

    }

    @Override
    public void onNeighborChange(ElectricalElementModule module, BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {

    }

    @Override
    public ElectricalElementModule createModule() {
        return new ElectricalElementModule();
    }
}
