package welbre.ambercraft.blocks.heat;

import io.netty.buffer.Unpooled;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.HeatPumpBE;
import welbre.ambercraft.client.AmberCraftScreenHelper;
import welbre.ambercraft.client.screen.ModifyFieldsScreen;
import welbre.ambercraft.module.ModuleFactory;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.heat.HeatModule;
import welbre.ambercraft.network.ModifyFieldsPayLoad;
import welbre.ambercraft.network.UpdateAmberSecureKeyPayload;

import java.util.UUID;

public class HeatPumpBlock extends Block implements EntityBlock {
    public static final ModuleFactory<HeatModule, HeatPumpBE> COLD_FACTORY = new ModuleFactory<>(HeatPumpBE.class,
            AmberCraft.Modules.HEAT_MODULE_TYPE,
            module -> {module.alloc(); module.getHeatNode().setThermalConductivity(100.0); module.getHeatNode().setThermalMass(30.0);},
            HeatModule::free,
            HeatPumpBE::setColdModule,
            HeatPumpBE::getColdModule
            ).setConstructor(HeatModule::init);
    public static final ModuleFactory<HeatModule, HeatPumpBE> HOT_FACTORY = COLD_FACTORY.copy().setGetter(HeatPumpBE::getHotModule).setSetter(HeatPumpBE::setHotModule);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public HeatPumpBlock(Properties p_49795_) {
        super(p_49795_);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!state.is(oldState.getBlock()))
        {
            COLD_FACTORY.create(level, pos);
            HOT_FACTORY.create(level, pos);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()))
        {
            COLD_FACTORY.destroy(level, pos);
            HOT_FACTORY.destroy(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        //in the case that a lever is used to check the temperature, concat the string as one.
        if (stack.getItem() == Items.LEVER)
        {
            if (level.isClientSide)
                return InteractionResult.SUCCESS;
            else
            {
                var cold = COLD_FACTORY.getModuleOn(level, pos).orElseThrow();
                var hot = HOT_FACTORY.getModuleOn(level, pos).orElseThrow();
                player.displayClientMessage(Component.literal(hot.getMultimeterString()).withColor(DyeColor.ORANGE.getTextColor()).append(" ").append(Component.literal(cold.getMultimeterString()).withColor(DyeColor.LIGHT_BLUE.getTextColor())), false);
                return InteractionResult.SUCCESS;
            }
        }
        var result = COLD_FACTORY.getType().useItemOn(COLD_FACTORY.getModuleOn(level,pos).orElse(null),stack,state,level,pos,player,hand,hitResult);
        if (result.consumesAction())
            return result;
        result = HOT_FACTORY.getType().useItemOn(HOT_FACTORY.getModuleOn(level,pos).orElse(null),stack,state,level,pos,player,hand,hitResult);
        if (result.consumesAction())
            return result;
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        var dir = Direction.getApproximateNearest(entity.position().subtract(pos.getX(),pos.getY(),pos.getZ()));
        if (state.getValue(FACING) == dir)
            HOT_FACTORY.getType().stepOn(HOT_FACTORY.getModuleOn(level,pos).orElse(null),level,pos,state,entity);
        else if (state.getValue(FACING) == dir.getOpposite())
            COLD_FACTORY.getType().stepOn(COLD_FACTORY.getModuleOn(level,pos).orElse(null),level,pos,state,entity);
        super.stepOn(level, pos, state, entity);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.isCreative())
            return InteractionResult.PASS;

        if (level.isClientSide)
            AmberCraftScreenHelper.openInClient(AmberCraftScreenHelper.TYPES.MODIFY_FIELDS, ModifyFieldsScreen.GET_BUFFER(pos, "power"), (LocalPlayer) player);
        else
        {
            var serverPlayer = (ServerPlayer) player;
            var entity = level.getBlockEntity(pos);
            if (entity == null)
                return InteractionResult.FAIL;

            serverPlayer.connection.send(ClientboundBlockEntityDataPacket.create(entity));
            UpdateAmberSecureKeyPayload.ADD_NEW_KEY(UUID.randomUUID(), pos, entity.getClass(), serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return ModulesHolder::TICK_HELPER;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HeatPumpBE(pos,state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        var state = getStateDefinition().any();
        return state.setValue(FACING, context.getClickedFace());
    }
}
