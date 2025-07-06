package welbre.ambercraft.module;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.registries.DeferredHolder;
import welbre.ambercraft.Main;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Defines how a module behavior.
 * @param <T> The module class to be created
 */
public interface ModuleType<T extends Module> {
    InteractionResult useWithoutItem(T module, BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult);
    InteractionResult useItemOn(T module, ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult);
    void stepOn(T module, Level level, BlockPos pos, BlockState state, Entity entity);

    T createModule();

    final class Template<T extends Module> implements Supplier<T> {
        private final ResourceLocation key;
        private final Consumer<T> setter;

        private Template(ResourceLocation key, Consumer<T> setter)
        {
            this.key = key;
            this.setter = setter;
        }

        public Template(DeferredHolder<ModuleType<?>, ? extends ModuleType<?>> holder, Consumer<T> setter) {
            this.key = holder.getId();
            this.setter = setter;
        }

        @Override
        public T get() {
            Optional<Holder.Reference<ModuleType<?>>> reference = Main.AmberRegisters.MODULE_TYPE_REGISTRY.get(key);
            if (reference.isPresent())
            {
                T module = (T) reference.get().getDelegate().value().createModule();
                setter.accept(module);
                return module;
            }
            return null;
        }

        public Template<T> changeSetter(Consumer<T> setter) {
            return new Template<>(this.key, setter);
        }
    }
}
