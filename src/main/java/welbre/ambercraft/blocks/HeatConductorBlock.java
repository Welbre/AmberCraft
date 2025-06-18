package welbre.ambercraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.blockentity.HeatConductorTile;
import welbre.ambercraft.blocks.parent.AmberBasicBlock;
import welbre.ambercraft.module.HeatModuleDefinition;
import welbre.ambercraft.module.ModularBlock;
import welbre.ambercraft.module.ModuleDefinition;

public abstract class HeatConductorBlock extends AmberBasicBlock implements ModularBlock, EntityBlock {
    private final HeatModuleDefinition heatModule = new HeatModuleDefinition();

    public HeatConductorBlock(Properties p) {
        super(p);
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
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return this::tick;
    }
}
