package welbre.ambercraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import welbre.ambercraft.blockentity.HeatSinkBlockEntity;
import welbre.ambercraft.blocks.parent.AmberBasicBlock;
import welbre.ambercraft.module.*;
import welbre.ambercraft.sim.heat.HeatNode;

public class HeatSinkBlock extends AmberBasicBlock implements EntityBlock {
    public static final VoxelShape shape = Shapes.box(0,0,0,1,13.0/16.0, 1);
    public HeatModuleDefinition heatModuleDefinition = new HeatModuleDefinition(HeatSinkBlock::SETTER);

    public HeatSinkBlock(Properties p) {
        super(p);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HeatSinkBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof HeatSinkBlockEntity sink)
        {
            if (stack.getItem() == Items.WATER_BUCKET) {
                if (sink.heatModule.getHeatNode().getTemperature() >= 100) {
                    if (!player.isCreative()) {
                        player.getInventory().removeItem(stack);
                        player.getInventory().add(new ItemStack(Items.BUCKET));
                    }
                    level.playLocalSound(
                            pos,
                            SoundEvents.FIRE_EXTINGUISH,
                            SoundSource.BLOCKS, 0.5f, 1f, false
                    );
                    sink.heatModule.getHeatNode().transferHeatToEnvironment(HeatNode.GET_AMBIENT_TEMPERATURE(level, pos), 30.0, HeatNode.DEFAULT_TIME_STEP);
                    return InteractionResult.SUCCESS;

                }
                return InteractionResult.CONSUME;
            }
            return heatModuleDefinition.useItemOn(sink.heatModule, stack,state,level,pos,player,hand, hitResult);
        }
        return super.useItemOn(stack,state,level,pos,player,hand,hitResult);
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    private static void SETTER(HeatNode node)
    {
        node.setEnvThermalConductivity(2.0);
        node.setThermalMass(10.0);
        node.setThermalConductivity(100.0);
    }
}
