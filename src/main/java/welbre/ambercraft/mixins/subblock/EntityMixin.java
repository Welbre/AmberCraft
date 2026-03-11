package welbre.ambercraft.mixins.subblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import welbre.ambercraft.subblock.SubBlock;

@Mixin(Entity.class)
public abstract class EntityMixin extends net.neoforged.neoforge.attachment.AttachmentHolder
{
    @Inject(method = "playStepSound", at = @At("HEAD"))
    protected void onPlayStepSoundHead(BlockPos pos, BlockState state, CallbackInfo ci)
    {
        SubBlock.IS_REQUIRING_STEP_SOUND = true;
    }

    @Inject(method = "playStepSound", at = @At("TAIL"))
    protected void onPlayStepSoundTail(BlockPos pos, BlockState state, CallbackInfo ci)
    {
        SubBlock.IS_REQUIRING_STEP_SOUND = false;
    }
}
