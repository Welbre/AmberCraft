package welbre.ambercraft.module.electrical;

import kuse.welbre.sim.electrical.abstractt.Element;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.network.NetworkModule;

public abstract class ElectricalModule extends NetworkModule {
    public abstract Element[] compile();

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() == AmberCraft.Items.MULTIMETER.get())
        {
            if (!level.isClientSide)
                return AmberCraft.Items.MULTIMETER.get().handle(stack, (ServerPlayer) player, this);
            return InteractionResult.SUCCESS;
        } else if (stack.getItem() == AmberCraft.Items.OSCILLOSCOPE.get())
        {
            if (!level.isClientSide)
                return AmberCraft.Items.OSCILLOSCOPE.get().handle(stack, (ServerPlayer) player, this);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
