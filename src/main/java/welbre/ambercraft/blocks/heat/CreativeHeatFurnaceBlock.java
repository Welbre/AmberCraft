package welbre.ambercraft.blocks.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.CreativeHeatFurnaceBE;
import welbre.ambercraft.blockentity.HeatBE;
import welbre.ambercraft.blocks.FreeRotationBlock;
import welbre.ambercraft.module.ModuleFactory;
import welbre.ambercraft.module.heat.HeatModule;

public class CreativeHeatFurnaceBlock extends FreeRotationBlock implements EntityBlock {
    public static final ModuleFactory<HeatModule, CreativeHeatFurnaceBE> factory = new ModuleFactory<>(
            CreativeHeatFurnaceBE.class,
            AmberCraft.Modules.HEAT_MODULE_TYPE,
            HeatModule::alloc,
            HeatModule::free,
            HeatBE::setHeatModule,
            HeatBE::getHeatModule
    ).setConstructor(HeatModule::init);

    public CreativeHeatFurnaceBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        factory.destroy(level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        factory.create(level, pos);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        factory.getModuleOn(level, pos).ifPresent(m -> factory.getType().stepOn(m, level, pos, state, entity));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        var result = factory.getType().useWithoutItem(factory.getModuleOn(level,pos).orElse(null), state, level, pos, player, hitResult);
        if (result.consumesAction())
            return result;
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        var result = factory.getType().useItemOn(factory.getModuleOn(level,pos).orElse(null),stack,state,level,pos,player,hand,hitResult);
        if (result.consumesAction())
            return result;
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CreativeHeatFurnaceBE(pos, state);
    }
}
