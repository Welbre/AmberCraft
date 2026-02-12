package welbre.ambercraft.subblock;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Definition that creates the parts that can be used in the SubBlock.<br>
 * This class is responsible for define logic, rendering, shape, external output, and more.
 * <h6 color="yellow">Must be registered in you TinyBlockRegister!</h>
 */
public abstract class TinyBlock
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

    /**
     * Get a list of squads used in the <b>static</b> render process.
     * <p>
     * The <b>static</b> rendering is similar of {@link net.minecraft.client.resources.model.BakedModel BakedModel},
     * they are required to pass a model to the SubBlock, and to minecraft render pipeline in the end.
     * </p>
     * <p color = "yellow">
     * Notice that the result of this function is stored in the SubBlock, if you TinyBlockState change,
     * and you want to update the model,you must call {@link SubBlockBE#requireStaticRenderUpdate(TinyBlockState)}
     * </p>
     *
     * @param state      the state that will be created a model by this TinyBlock.
     * @param cull       The direction that requires a modeling.
     * @param rand
     * @param extraData
     * @param renderType
     * @return All squads used in the model
     */
    public abstract List<BakedQuad> staticRender(TinyBlockState state, Direction cull, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType);

    public abstract void dynamicRender(TinyBlockState state);

    /// Return an itemStack or null based on the parameters.<br>
    /// Use it for drop itemStack when breaks, exploded, broken with wrong tool, or others situations.
    //  todo implement all the parameters need to decide the item
    public abstract @Nullable ItemStack getDroppedItem();
}
