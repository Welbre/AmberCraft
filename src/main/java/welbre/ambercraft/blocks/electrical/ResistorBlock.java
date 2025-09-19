package welbre.ambercraft.blocks.electrical;


import kuse.welbre.sim.electrical.elements.Resistor;
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
import welbre.ambercraft.blockentity.electrical.DirectionalElectricalBE;
import welbre.ambercraft.blockentity.electrical.ElectricalBE;

public class ResistorBlock extends ElectricalBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public ResistorBlock(Properties p) {
        super(p);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
        factory.setConstructor(
                (module, entity, factory, level, pos) -> {
                    module.setElement(new Resistor(1));
                }
        );
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        var result = super.useWithoutItem(state, level, pos, player, hitResult);
        if (result.consumesAction())
            return result;
        if (level.getBlockEntity(pos) instanceof ElectricalBE element && player.getMainHandItem().is(Items.AIR))
        {
            if (!level.isClientSide)
            {
                if ( element.getElement() instanceof Resistor resistor)
                {
                    final double resistance = resistor.getResistance() * (player.isShiftKeyDown() ? 0.5 : 2);
                    resistor.setResistance(Math.max(10e-6, resistance));//1microOhm of min resistence
                    element.setChanged();
                    element.getElectricalModule().dirtMaster();
                    level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
                    ((ServerPlayer) player).sendSystemMessage(Component.literal("Resistance set to: " + resistance).withColor(DyeColor.ORANGE.getTextColor()));
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
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
