package welbre.ambercraft.subblock.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.subblock.SubBlockBE;
import welbre.ambercraft.subblock.TinyBlock;
import welbre.ambercraft.subblock.TinyBlockState;

public final class SubBlockClientExtensions implements IClientBlockExtensions
{
    @Override
    public boolean addDestroyEffects(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull ParticleEngine manager)
    {
        if (level.getBlockEntity(pos) instanceof SubBlockBE sub)
        {
            TinyBlockState isBreaking = sub.getPlayerIsBreaking();
            if (isBreaking != null)
                isBreaking.definition.handleParticles((ClientLevel) level, pos, manager, TinyBlock.ParticleCase.DESTROY);
        }
        return true;//don't spawn particles at all
    }

    @Override
    public boolean playBreakSound(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos)
    {
        return IClientBlockExtensions.super.playBreakSound(state, level, pos);
    }
}
