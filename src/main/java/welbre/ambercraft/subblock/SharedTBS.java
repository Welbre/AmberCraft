package welbre.ambercraft.subblock;

import net.minecraft.core.BlockPos;

/**
 * Used when a {@link TinyBlockState} is shared across multiples {@link SubBlock}.
 */
public record SharedTBS(BlockPos owner, TinyBlockState sharedState)
{




}
