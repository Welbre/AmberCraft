package welbre.ambercraft.module;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Defines how a {@link Module} behavior.<br>
 * To create your one ModulesType,
 * instantiate this interface and implement all methods, before that,
 * register the ModuleType in the {@link welbre.ambercraft.Main.Modules#REGISTER REGISTER}.<br>
 * The interactions with the Block and BlockEntity isn't handled automatically by AmberCraft or by NeoForge,
 * Remember to call the functions in your custom Blocks.
 * @param <T> The module class to be created.
 */
public interface ModuleType<T extends Module> {
    InteractionResult useWithoutItem(T module, BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult);
    InteractionResult useItemOn(T module, ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult);
    void stepOn(T module, Level level, BlockPos pos, BlockState state, Entity entity);

    T createModule();
}
