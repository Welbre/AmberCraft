package welbre.ambercraft.subblock;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Definition that creates the parts that can be used in the SubBlock.<br>
 * This class is responsible for define logic, rendering, shape, external output and more.
 * <h6 color="yellow">Must be registered in you TinyBlockRegister!</h>
 */
public class TinyBlock
{
    public VoxelShape shape;
    public final ResourceLocation registerName;

    public TinyBlock(ResourceLocation registerName)
    {
        this.registerName = registerName;
    }

    public TinyBlock(ResourceLocation l, VoxelShape shape)
    {
        this.registerName = l;
        this.shape = shape;
    }
}
