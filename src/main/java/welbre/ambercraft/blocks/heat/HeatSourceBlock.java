package welbre.ambercraft.blocks.heat;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.HeatBE;
import welbre.ambercraft.blockentity.HeatSourceBE;
import welbre.ambercraft.menu.HeatSourceScreen;
import welbre.ambercraft.module.ModuleFactory;
import welbre.ambercraft.module.heat.HeatModule;

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
        return HeatSourceBE::TICK;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof HeatSourceBE source)
        {
            if (level.isClientSide)
                Minecraft.getInstance().setScreen(new HeatSourceScreen(source));
            else
                ((ServerPlayer) player).connection.send(ClientboundBlockEntityDataPacket.create(source));
        }
        return InteractionResult.SUCCESS;
    }
}
