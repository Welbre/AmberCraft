package welbre.ambercraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.blocks.parent.AmberBasicBlock;
import welbre.ambercraft.module.HeatModuleDefinition;
import welbre.ambercraft.module.ModularBlock;
import welbre.ambercraft.module.ModuleDefinition;

public abstract class HeatConductorBlock extends AmberBasicBlock implements ModularBlock, EntityBlock {
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty EAST = BooleanProperty.create("east");

    private final HeatModuleDefinition heatModule = new HeatModuleDefinition();

    public HeatConductorBlock(Properties p) {
        super(p);
        p.sound(SoundType.METAL);
        p.noOcclusion();
        registerDefaultState(getStateDefinition().any()
                .setValue(UP,false)
                .setValue(DOWN,false)
                .setValue(NORTH,false)
                .setValue(SOUTH,false)
                .setValue(WEST,false)
                .setValue(EAST,false)
        );
    }

    @Override
    public ModuleDefinition[] getModuleDefinition() {
        return new ModuleDefinition[]{heatModule};
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return heatModule.useItemOn(stack,state,level,pos,player,hand,hitResult);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        heatModule.stepOn(level,pos,state,entity);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(UP,DOWN,NORTH,SOUTH,WEST,EAST);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return calculateState(context.getLevel(), context.getClickedPos(), defaultBlockState());
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return this::tick;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        level.setBlockAndUpdate(pos, calculateState(level, pos, state));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        level.setBlockAndUpdate(pos, calculateState(level,pos, state));
    }

    private BlockState calculateState(Level level, BlockPos pos, BlockState state){
        for (Direction dir : Direction.values()) {
            BooleanProperty property = null;
            switch (dir){
                case UP -> property = UP;
                case DOWN -> property = DOWN;
                case NORTH -> property = NORTH;
                case SOUTH -> property = SOUTH;
                case WEST -> property = WEST;
                case EAST -> property = EAST;
            }
            BlockState relative = level.getBlockState(pos.relative(dir));
            if (relative.getBlock() instanceof HeatConductorBlock){
                state = state.setValue(property, Boolean.TRUE);
            }
        }
        return state;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.create(
                new AABB(new Vec3(0.5f - 0.4/2f,0.5f - 0.4/2f,0.5f - 0.4/2f), new Vec3(0.5f + 0.4/2f,0.5f + 0.4/2f,0.5f + 0.4/2f))
        );
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return super.getRenderShape(state);
    }
}
