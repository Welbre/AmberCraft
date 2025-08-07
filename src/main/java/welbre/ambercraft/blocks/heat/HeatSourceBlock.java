package welbre.ambercraft.blocks.heat;

import io.netty.buffer.Unpooled;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.blockentity.HeatSourceBE;
import welbre.ambercraft.client.AmberCraftScreenHelper;
import welbre.ambercraft.module.ModulesHolder;

public class HeatSourceBlock extends HeatBlock {
    public HeatSourceBlock(Properties p_49795_) {
        super(p_49795_);
        factory.setConstructor(
                (module, entity, factory, level, pos) -> {
                    module.init(entity, factory, level, pos);
                    if (entity instanceof HeatSourceBE source)
                        source.temperature = module.getHeatNode().getTemperature();
                }
        );
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HeatSourceBE(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return ModulesHolder::TICK_HELPER;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof HeatSourceBE source)
        {
            if (level.isClientSide)
                AmberCraftScreenHelper.openInClient(AmberCraftScreenHelper.TYPES.HEAT_SOURCE, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(pos), (LocalPlayer) player);
            else
                ((ServerPlayer)player).connection.send(ClientboundBlockEntityDataPacket.create(source));
        }
        return InteractionResult.SUCCESS;
    }
}
