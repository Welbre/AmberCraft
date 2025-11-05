package welbre.ambercraft.blocks.electrical;


import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.electrical.InsulatorBE;
import welbre.ambercraft.module.ModuleFactory;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.electrical.ElectricalCableModule;

import java.util.HashMap;
import java.util.UUID;

public class InsulatorBlock extends Block implements EntityBlock
{
    public ModuleFactory<ElectricalCableModule, InsulatorBE> factory = new ModuleFactory<>(
            InsulatorBE.class,
            AmberCraft.ModuleTypes.ELECTRICAL_CABLE_MODULE_TYPE,
            ElectricalCableModule::alloc,
            ElectricalCableModule::free,
            InsulatorBE::setCableModule,
            InsulatorBE::getCableModule
    );

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InsulatorBE(pos,state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return ModulesHolder::TICK_HELPER;
    }

    public InsulatorBlock(Properties p_49795_) {
        super(p_49795_);
        factory.setConstructor((module, entity, factory, level, pos) -> {
            module.setResistence(0.5);
        });
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        factory.create(level, pos);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        factory.destroy(level, pos);
        if (level.getBlockEntity(pos) instanceof InsulatorBE insulator)
        {
            //if this insulator is connected, then for each cable on it, go to the other insulator and remove "this" position from the cable list
            if (insulator.isConnected())
            {
                for (BlockPos cablePos : insulator.getCablePos())
                {
                    if (level.getBlockEntity(cablePos) instanceof InsulatorBE other)
                    {
                        other.removeCablePos(pos);
                        other.setChanged();
                        level.sendBlockUpdated(cablePos, level.getBlockState(cablePos), level.getBlockState(cablePos), Block.UPDATE_ALL);
                    }
                }
            }

        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public static final HashMap<UUID, BlockPos> clicked = new HashMap<>();

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult)
    {
        if (level.getBlockEntity(pos) instanceof InsulatorBE insulator && player.getMainHandItem().isEmpty() && !level.isClientSide)
        {
            if (clicked.containsKey(player.getUUID()))
            {
                BlockPos otherPos = clicked.remove(player.getUUID());
                if (level.getBlockEntity(otherPos) instanceof InsulatorBE otherInsulator)
                {
                    otherInsulator.getCableModule().connect(insulator.getCableModule());

                    otherInsulator.addCablePos(pos);
                    insulator.addCablePos(otherPos);

                    otherInsulator.setChanged();
                    insulator.setChanged();
                    level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
                    level.sendBlockUpdated(otherPos, state, state, Block.UPDATE_ALL);

                    ((ServerPlayer) player).sendSystemMessage(Component.literal("Cable set!"));
                }
            }
            else
            {
                ((ServerPlayer) player).sendSystemMessage(Component.literal("First clicked"));
                clicked.put(player.getUUID(), pos);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context)
    {
        return Shapes.box(6f/16f,0,6f/16f,10f/16f, 0.5 ,10f/16f);
    }
}
