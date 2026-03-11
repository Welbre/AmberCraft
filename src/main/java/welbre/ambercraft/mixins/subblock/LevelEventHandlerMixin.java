package welbre.ambercraft.mixins.subblock;

import net.minecraft.client.renderer.LevelEventHandler;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import welbre.ambercraft.subblock.SubBlock;

@Mixin(LevelEventHandler.class)
public class LevelEventHandlerMixin
{
    
    @Inject(method = "levelEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/level/block/SoundType;"
            )
    )
    public void onLevelEventBefore(int type, BlockPos pos, int data, CallbackInfo ci)
    {
        SubBlock.IS_REQUIRING_BREAKING_SOUND = true;
    }

    @Inject(method = "levelEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/level/block/SoundType;",
                    shift = At.Shift.AFTER
            )
    )
    public void onLevelEventAfter(int type, BlockPos pos, int data, CallbackInfo ci)
    {
        SubBlock.IS_REQUIRING_BREAKING_SOUND = false;
    }
}
