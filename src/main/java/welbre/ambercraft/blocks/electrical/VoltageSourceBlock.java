package welbre.ambercraft.blocks.electrical;


import kuse.welbre.sim.electrical.elements.VoltageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
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
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.blockentity.electrical.VoltageSourceBE;

public class VoltageSourceBlock extends ElectricalBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public VoltageSourceBlock(Properties p) {
        super(p);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.getBlockEntity(pos) instanceof VoltageSourceBE vs)
            vs.electricalModule.setElement(new VoltageSource(0));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        var result = super.useWithoutItem(state, level, pos, player, hitResult);
        if (result.consumesAction())
            return result;
        if (level.getBlockEntity(pos) instanceof VoltageSourceBE source && player.getMainHandItem().is(Items.AIR))
        {
            if (!level.isClientSide)
            {
                final double voltage = source.getElement().getVoltageDifference() + (player.isShiftKeyDown() ? -10 : 10);
                ((VoltageSource) source.getElement()).setSourceVoltage(voltage);
                source.setChanged();
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
                ((ServerPlayer) player).sendSystemMessage(Component.literal("Voltage set to: " + voltage).withColor(DyeColor.ORANGE.getTextColor()));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VoltageSourceBE(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
    }
}
