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
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.blocks.parent.AmberBasicBlock;
import welbre.ambercraft.module.HeatModuleDefinition;
import welbre.ambercraft.module.ModularBlock;
import welbre.ambercraft.module.ModuleDefinition;

public abstract class HeatConductorBlock extends AmberBasicBlock implements ModularBlock, EntityBlock {
    public static final float MODEL_SIZE = 0.4f;

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
    public ModuleDefinition[] getModuleDefinition(BlockState state, Direction direction) {
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
        return calculateState(context.getLevel(), context.getClickedPos());
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return this::tick;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        level.setBlockAndUpdate(pos, calculateState(level, pos));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        level.setBlockAndUpdate(pos, calculateState(level,pos));
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        return calculateState((Level) level,pos);
    }

    private BlockState calculateState(Level level, BlockPos pos){
        BlockState state = defaultBlockState();
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
            if (relative.getBlock() instanceof ModularBlock modular)
            {
                for (ModuleDefinition definition : modular.getModuleDefinition(relative, dir.getOpposite()))
                {
                    if (definition instanceof HeatModuleDefinition)
                        state = state.setValue(property, Boolean.TRUE);
                }
            }
        }
        return state;
    }

    @Override
    protected VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        AABB c = AABB.ofSize(new Vec3(.5, .5, .5), MODEL_SIZE, MODEL_SIZE, MODEL_SIZE);//center
        VoxelShape shape = Shapes.create(c);
        if (state.getValue(HeatConductorBlock.UP))
            shape = Shapes.join(shape, Shapes.box(c.minX, c.maxY, c.minZ, c.maxX, 1f, c.maxZ),BooleanOp.OR);
        if (state.getValue(HeatConductorBlock.DOWN))
            shape = Shapes.join(shape, Shapes.box(c.minX, 0, c.minZ, c.maxX, c.minY, c.maxZ),BooleanOp.OR);
        if (state.getValue(HeatConductorBlock.NORTH))
            shape = Shapes.join(shape, Shapes.box(c.minX, c.minY, 0, c.maxX, c.maxY, c.minZ),BooleanOp.OR);
        if (state.getValue(HeatConductorBlock.SOUTH))
            shape = Shapes.join(shape, Shapes.box(c.minX, c.minY, c.maxZ, c.maxX, c.maxY, 1f),BooleanOp.OR);
        if (state.getValue(HeatConductorBlock.WEST))
            shape = Shapes.join(shape, Shapes.box(0, c.minY, c.minZ, c.minX, c.maxY, c.maxZ),BooleanOp.OR);
        if (state.getValue(HeatConductorBlock.EAST))
            shape = Shapes.join(shape, Shapes.box(c.maxX, c.minY, c.minZ, 1f, c.maxY, c.maxZ),BooleanOp.OR);
        return shape;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return super.getRenderShape(state);
    }
}
