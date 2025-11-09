package welbre.ambercraft.blocks.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import welbre.ambercraft.blockentity.heat.HeatSinkBE;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.heat.HeatModule;
import welbre.ambercraft.sim.heat.HeatNode;

public class HeatSinkBlock extends HeatBlock<HeatSinkBE> implements EntityBlock {
    public static final VoxelShape shape = Shapes.box(0,0,0,1,13.0/16.0, 1);


    public HeatSinkBlock(Properties p) {
        super(p);
        moduleConstructor.push(
                HeatSinkBlock::SETUP_HEAT_SINK);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new HeatSinkBE(pos, state);
    }

    @Override
    protected @NotNull InteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        //use a water bucket in the heat sink if the temperature is above of 100 then trade heat and play a sound
        if (level.getBlockEntity(pos) instanceof HeatSinkBE sink)
        {
            if (stack.getItem() == Items.WATER_BUCKET)
            {
                if (level instanceof ServerLevel) {
                    if (sink.getHeatModule().getHeatNode().getTemperature() >= 100)
                    {
                        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 1f);
                        sink.getHeatModule().getHeatNode().computeSoftHeatToEnvironment(HeatNode.GET_AMBIENT_TEMPERATURE(level, pos), 30.0, 1.0);
                        return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(new ItemStack(Items.BUCKET));
                    }
                    else
                        return InteractionResult.SUCCESS_SERVER;
                }

                return InteractionResult.CONSUME;
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return shape;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        return ModulesHolder::TICK_HELPER;
    }

    private static void SETUP_HEAT_SINK(HeatModule module, HeatSinkBE holder, Level level, BlockPos pos) {
        HeatNode node = module.getHeatNode();
        node.setEnvThermalConductivity(2.0);
        node.setThermalMass(10.0);
        node.setThermalConductivity(100.0);
        node.setEnvConditions(HeatNode.GET_AMBIENT_TEMPERATURE(level, pos), 0.01);
    }
}
