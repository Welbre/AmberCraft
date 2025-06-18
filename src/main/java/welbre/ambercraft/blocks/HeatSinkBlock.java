package welbre.ambercraft.blocks;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.blockentity.HeatSinkBlockEntity;
import welbre.ambercraft.blocks.parent.AmberBasicBlock;
import welbre.ambercraft.module.HeatModuleDefinition;
import welbre.ambercraft.module.ModularBlock;
import welbre.ambercraft.module.ModuleDefinition;

public class HeatSinkBlock extends AmberBasicBlock implements ModularBlock, EntityBlock {
    public static final VoxelShape shape = Shapes.box(0,0,0,1,13.0/16.0, 1);
    public HeatModuleDefinition heatModuleDefinition = new HeatModuleDefinition();

    public HeatSinkBlock(Properties p) {
        super(p);
    }

    @Override
    public ModuleDefinition[] getModuleDefinition() {
        return new ModuleDefinition[]{heatModuleDefinition};
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HeatSinkBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return this::tick;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return heatModuleDefinition.useItemOn(stack,state,level,pos,player,hand, hitResult);
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }
}
