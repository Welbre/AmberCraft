package welbre.ambercraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
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
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.HeatSinkBE;
import welbre.ambercraft.blocks.parent.AmberBasicBlock;
import welbre.ambercraft.module.ModuleFactory;
import welbre.ambercraft.module.heat.HeatModule;
import welbre.ambercraft.sim.heat.HeatNode;

public class HeatSinkBlock extends AmberBasicBlock implements EntityBlock {
    public static final VoxelShape shape = Shapes.box(0,0,0,1,13.0/16.0, 1);
    public ModuleFactory<HeatModule,HeatSinkBE> factory = new ModuleFactory<>(
            HeatSinkBE.class,
            AmberCraft.Modules.HEAT_MODULE_TYPE,
            HeatSinkBlock::MODULE_INIT,
            HeatModule::free,
            HeatSinkBE::setHeatModule,
            HeatSinkBE::getHeatModule
    ).setConstructor((a,b,c,d,e) -> {
        a.init(b,c,d,e);
        a.getHeatNode().setEnvConditions(HeatNode.GET_AMBIENT_TEMPERATURE(d,e),0.01);
    });

    public HeatSinkBlock(Properties p) {
        super(p);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HeatSinkBE(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof HeatSinkBE sink)
        {
            if (stack.getItem() == Items.WATER_BUCKET)
            {
                if (level instanceof ServerLevel) {
                    if (sink.getHeatModule().getHeatNode().getTemperature() >= 100)
                    {
                        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 1f);
                        sink.getHeatModule().getHeatNode().computeSoftHeatToEnvironment(HeatNode.GET_AMBIENT_TEMPERATURE(level, pos), 30.0, HeatNode.DEFAULT_TIME_STEP);
                        return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(new ItemStack(Items.BUCKET));
                    }
                    else
                        return InteractionResult.SUCCESS_SERVER;
                }

                return InteractionResult.CONSUME;
            }

            var result = factory.getType().useItemOn(sink.getHeatModule(), stack, state, level, pos, player, hand, hitResult);
            if (result.consumesAction())
                return result;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (level.getBlockEntity(pos) instanceof HeatSinkBE sink)
            factory.getType().stepOn(sink.getHeatModule(), level, pos, state, entity);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        factory.create(level,pos);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        factory.destroy(level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return HeatSinkBE::TICK;
    }

    private static void MODULE_INIT(HeatModule module)
    {
        module.alloc();
        var node = module.getHeatNode();
        node.setEnvThermalConductivity(2.0);
        node.setThermalMass(10.0);
        node.setThermalConductivity(100.0);
    }
}
