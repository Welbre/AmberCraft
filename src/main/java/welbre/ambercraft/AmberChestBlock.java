package welbre.ambercraft;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class AmberChestBlock extends Block implements EntityBlock
{
    public AmberChestBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new AmberChestBE(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult)
    {
        if (level instanceof ServerLevel server)
        {
            IItemHandler capability = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
            if (player instanceof ServerPlayer serverPlayer)
            {
                serverPlayer.sendSystemMessage(Component.literal(capability.getStackInSlot(0).toString()));
            }
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }
}
