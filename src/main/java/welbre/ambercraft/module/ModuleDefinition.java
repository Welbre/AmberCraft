package welbre.ambercraft.module;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.fml.common.Mod;

/**
 * Defines how a module behavior.
 * @param <T> The module class to be created
 * @param <K> The data needed to create the module
 */
public interface ModuleDefinition<T extends Module, K> {
    InteractionResult useWithoutItem(T module, BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult);
    InteractionResult useItemOn(T module, ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult);
    void stepOn(T module, Level level, BlockPos pos, BlockState state, Entity entity);
    T createModule(K obj);
}
