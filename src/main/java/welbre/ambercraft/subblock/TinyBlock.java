package welbre.ambercraft.subblock;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
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

    /// returns the AABB with the bounds translated
    protected AABB getTranslatedBounds(TinyBlockState state)
    {
        return shape.bounds().move(state.x / 16.0, state.y / 16.0, state.z / 16.0);
    }

    /// Solves the shape ande return a translated list of AABB
    protected List<AABB> getTranslatedAABB(TinyBlockState state)
    {
        return shape.toAabbs().stream().map(aabb -> aabb.move(state.x / 16.0, state.y / 16.0, state.z / 16.0)).toList();
    }

    /**
     * Gets a model for <b>static</b> rendering the item or in sub block.<br>
     *
     * <p>
     *     Static rendering is a concept used in AmberCraft subBlocks created to be similar to Minecraft's model pipe-line for blocks.<br>
     *     The result of this method is stored in an internal cache <i>in SubBlockBE</i>, and reused many times, to force the update of the model use {@link SubBlockBE#requireStaticRenderUpdate(TinyBlockState)}.<br>
     *     You don't need to call this my hand in most cases, the SubBlock do by him self when player interact with the SubBlock.
     * </p>
     *<p>
     *     The TinyBlockState state parameter is the block state that need a model to be updated. <b>The state can be null!</b> and this only happen if and only if the method is called to get an <b>ITEM!</b> model.
     *</p>
     */
    public abstract BakedModel staticModel(@Nullable TinyBlockState state);

    public abstract void dynamicRender(TinyBlockState state);

    /// Return an itemStack or null based on the parameters.<br>
    /// Use it for drop itemStack when breaks, exploded, broken with wrong tool, or others situations.
    //  todo implement all the parameters need to decide the item
    public abstract @Nullable ItemStack getDroppedItem();

    public abstract @NotNull Component getTinyItemName();
}
