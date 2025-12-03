package welbre.ambercraft.blocks.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.heat.HeatPumpBE;
import welbre.ambercraft.client.AmberCraftScreenHelper;
import welbre.ambercraft.client.screen.ModifyFieldsScreen;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.heat.HeatModule;
import welbre.ambercraft.network.UpdateAmberSecureKeyPayload;

import java.util.Stack;

public class HeatPumpBlock extends Block implements EntityBlock {
    public Stack<Module.Consumer<HeatPumpBE, HeatModule>> moduleConstructor = new Stack<>();
    public Stack<Module.Consumer<HeatPumpBE, HeatModule>> moduleDestructor = new Stack<>();

    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public HeatPumpBlock(Properties p_49795_) {
        super(p_49795_);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
        moduleConstructor.push(HeatModule::ALLOC_MODULE_CONSUMER);
        moduleConstructor.push(HeatModule::init);
        moduleConstructor.push((module, holder, level, pos) -> {
            module.getHeatNode().setThermalConductivity(100.0);
            module.getHeatNode().setThermalMass(30.0);
        });
        moduleDestructor.push(HeatModule::PRE_FREE_MODULE_CONSUMER);
    }

    @Override
    protected void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!state.is(oldState.getBlock()))
        {
            Module.executeInLevel(HeatPumpBE.class, level, pos, HeatPumpBE::getHotModule, moduleConstructor);
            Module.executeInLevel(HeatPumpBE.class, level, pos, HeatPumpBE::getColdModule, moduleConstructor);
        }
    }

    @Override
    protected void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()))
        {
            Module.executeInLevel(HeatPumpBE.class, level, pos, HeatPumpBE::getHotModule, moduleDestructor);
            Module.executeInLevel(HeatPumpBE.class, level, pos, HeatPumpBE::getColdModule, moduleDestructor);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected @NotNull InteractionResult useItemOn(
            ItemStack stack,
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull Player player,
            @NotNull InteractionHand hand,
            @NotNull BlockHitResult hitResult)
    {
        //the HeatModule already checks for thermometer used in the block, but in this case
        //we have 2 modules in on block, and I want that only one single mensagem to be sent.
        if (stack.getItem() == AmberCraft.Items.THERMOMETER.get())
        {
            if (!level.isClientSide)
            {
                HeatModule cold = Module.getInLevel(HeatPumpBE.class, level, pos, HeatPumpBE::getColdModule);
                HeatModule hot = Module.getInLevel(HeatPumpBE.class, level, pos, HeatPumpBE::getHotModule);
                if (hot == null || cold == null)
                    return InteractionResult.PASS;
                player.displayClientMessage(Component.literal(hot.getMultimeterString()).withColor(DyeColor.ORANGE.getTextColor()).append(" ").append(Component.literal(cold.getMultimeterString()).withColor(DyeColor.LIGHT_BLUE.getTextColor())), false);
            }
            return InteractionResult.SUCCESS;
        }

        InteractionResult coldResult = Module.HANDLE_USE_ITEM_ON(HeatPumpBE.class, HeatPumpBE::getColdModule, stack, state, level, pos, player, hand, hitResult);
        if (coldResult != null && coldResult.consumesAction())
            return coldResult;
        InteractionResult hotResult = Module.HANDLE_USE_ITEM_ON(HeatPumpBE.class, HeatPumpBE::getHotModule, stack, state, level, pos, player, hand, hitResult);
        if (hotResult != null && hotResult.consumesAction())
            return hotResult;

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public void stepOn(@NotNull Level level, BlockPos pos, BlockState state, Entity entity) {
        var dir = Direction.getApproximateNearest(entity.position().subtract(pos.getX(),pos.getY(),pos.getZ()));
        if (state.getValue(FACING) == dir)
            Module.HANDLE_STEP_ON(HeatPumpBE.class, HeatPumpBE::getHotModule, level, pos, state, entity);
        else if (state.getValue(FACING) == dir.getOpposite())
            Module.HANDLE_STEP_ON(HeatPumpBE.class, HeatPumpBE::getColdModule, level, pos, state, entity);
        super.stepOn(level, pos, state, entity);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, Player player, @NotNull BlockHitResult hitResult) {
        if (!player.isCreative())
            return InteractionResult.PASS;

        if (level.isClientSide)
            AmberCraftScreenHelper.openInClient(AmberCraftScreenHelper.TYPES.MODIFY_FIELDS, ModifyFieldsScreen.GET_BUFFER(pos, "power"));
        else
        {
            var serverPlayer = (ServerPlayer) player;
            var entity = level.getBlockEntity(pos);
            if (entity == null)
                return InteractionResult.FAIL;

            serverPlayer.connection.send(ClientboundBlockEntityDataPacket.create(entity));
            UpdateAmberSecureKeyPayload.ADD_NEW_KEY(pos, entity.getClass(), serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        return ModulesHolder::TICK_HELPER;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new HeatPumpBE(pos,state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        var state = getStateDefinition().any();
        return state.setValue(FACING, context.getClickedFace());
    }
}
