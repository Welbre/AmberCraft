package welbre.ambercraft.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;

public class BasicAmberBlock extends Block {

    public BasicAmberBlock(Properties p) {
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
