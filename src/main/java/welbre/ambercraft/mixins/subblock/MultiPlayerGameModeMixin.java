package welbre.ambercraft.mixins.subblock;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import welbre.ambercraft.subblock.SubBlock;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin
{
    @Inject(method = "continueDestroyBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/level/block/SoundType;"
            )
    )
    public void onContinueDestroyBlockBefore(BlockPos posBlock, Direction directionFacing, CallbackInfoReturnable<Boolean> cir)
    {
        SubBlock.IS_REQUIRING_HIT_SOUND = true;
    }

    @Inject(method = "continueDestroyBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/level/block/SoundType;",
                    shift = At.Shift.AFTER
            )
    )
    public void onContinueDestroyBlockAfter(BlockPos posBlock, Direction directionFacing, CallbackInfoReturnable<Boolean> cir)
    {
        SubBlock.IS_REQUIRING_HIT_SOUND = true;
    }
}
