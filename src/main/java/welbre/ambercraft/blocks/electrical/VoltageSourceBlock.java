package welbre.ambercraft.blocks.electrical;


import io.netty.buffer.Unpooled;
import kuse.welbre.sim.electrical.elements.VoltageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.blockentity.electrical.DirectionalElectricalBE;
import welbre.ambercraft.blockentity.electrical.ElectricalBE;
import welbre.ambercraft.client.AmberCraftScreenHelper;
import welbre.ambercraft.client.screen.VoltageSourceScreen;
import welbre.ambercraft.network.AmberCraftScreenOpenerPayload;
import welbre.ambercraft.network.UpdateAmberSecureKeyPayload;

import java.util.UUID;

public class VoltageSourceBlock extends ElectricalBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public VoltageSourceBlock(Properties p) {
        super(p);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
        factory.setConstructor((module, entity, factory, level, pos) -> {
            module.setElement(new VoltageSource(0));
        });
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        var result = super.useWithoutItem(state, level, pos, player, hitResult);
        if (result.consumesAction())
            return result;
        if (level.getBlockEntity(pos) instanceof ElectricalBE source && player.getMainHandItem().is(Items.AIR))
        {
            if (!level.isClientSide)
            {
                AmberCraftScreenHelper.openInClient(
                        AmberCraftScreenHelper.TYPES.VOLTAGE_SOURCE_SETTINGS,
                        VoltageSourceScreen.CREATE_BUFFER(level, pos),
                        (ServerPlayer) player
                );
                //send a new key to the client to modify the block via AmberCraftVoltageSourceModifierPayload
                UpdateAmberSecureKeyPayload.ADD_NEW_KEY(pos, source.getClass(), (ServerPlayer) player);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DirectionalElectricalBE(pos,state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown())
            return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
        else
            return this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
    }
}
