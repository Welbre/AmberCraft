package welbre.ambercraft.blocks.parent;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;

public class AmberBasicBlock extends Block {

    public AmberBasicBlock(Properties p) {
        super(p
                .sound(SoundType.STONE)
                .strength(0.3F)
                .noOcclusion()
                .isValidSpawn(Blocks::never)
                .isRedstoneConductor((a,b,c) -> false)
                .isSuffocating((a,b,c) -> false)
                .isViewBlocking((a,b,c) -> false)
        );
    }
}
