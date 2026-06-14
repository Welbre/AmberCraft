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
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.network.NetworkModule;

public abstract class ElectricalModule extends NetworkModule {
    /// run before the network compilation step, can be used to setups
    public void preCompile(@NotNull ElectricalMaster master){}

    /// returns all elements to be added in the circuit. can be used to perform setup but isn't recommended
    public abstract @NotNull Element[] compile();

    /// runs after the network compilation step, at this point all elements and the circuit is created, but the MNA matrix isn't ready yet.
    public void posCompile(@NotNull ElectricalMaster master){}

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
