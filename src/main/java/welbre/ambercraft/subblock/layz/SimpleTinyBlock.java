package welbre.ambercraft.subblock.layz;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.DelegateBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.subblock.TinyBlock;
import welbre.ambercraft.subblock.TinyBlockState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A lazy class created to represent Simple blocks, NO VARIANTS, CUBE, with only 1 measure an integer between 1 and 16
 */
public class SimpleTinyBlock extends TinyBlock
{
    public final Block block;
    public final int size;

    public SimpleTinyBlock(ResourceLocation registerName, int size, @NotNull Block block)
    {
        super(registerName);
        if (size < 1 || size > 16) throw new IllegalArgumentException("Size must be between 1 and 16");
        if (! block.getStateDefinition().getProperties().isEmpty()) throw new IllegalArgumentException("Block must not have variants!");

        this.shape = Shapes.box(0,0,0,size / 16.0, size / 16.0, size / 16.0);
        this.block = block;
        this.size = size;
    }

    @Override
    public BakedModel staticModel(@Nullable TinyBlockState state)
    {
        if (state != null)
            return new SimpleTinyBlockBakedModel(Minecraft.getInstance().getBlockRenderer().getBlockModel(block.defaultBlockState()), state.x / 16f, state.y / 16f, state.z / 16f, size / 16f);
        else
            return new SimpleTinyBlockBakedModel(Minecraft.getInstance().getBlockRenderer().getBlockModel(block.defaultBlockState()), 0, 0, 0, size / 16f);
    }

    @Override
    public void dynamicRender(TinyBlockState state)
    {

    }

    @Override
    public @NotNull Component getTinyItemName()
    {
        return Component.literal("%dx%dx%d Tiny %s".formatted(size, size, size, block.asItem().getName().getString()));
    }

    @Override
    public @Nullable ItemStack getDroppedItem() {
        //todo check the current Item to be dropped
        return block.asItem().getDefaultInstance();
    }

    @Override
    public @NotNull SoundType getSoundType(TinyBlockState state){
        //todo check if getSoundType(BlockState .......) can be used where
        return block.defaultBlockState().getSoundType();
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------Model management---------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------------------------------------


    /**
     * Used for create the model of tiny blocks.
     */
    public static final class SimpleTinyBlockBakedModel extends DelegateBakedModel
    {
        private final float x,y,z, scale;

        public SimpleTinyBlockBakedModel(BakedModel parent, float x, float y, float z, float scale)
        {
            super(parent);
            this.x = x;
            this.y = y;
            this.z = z;
            this.scale = scale;
        }

        @Override
        public @NotNull List<BakedQuad> getQuads(@Nullable BlockState p_371320_, @Nullable Direction p_371369_, @NotNull RandomSource p_371947_) {
            //called by the item render
            List<BakedQuad> quads = super.getQuads(p_371320_, p_371369_, p_371947_);
            return SCALE_AND_MOVE(quads, x, y, z, scale);
        }

        @Override
        public @NotNull List<BakedQuad> getQuads(@Nullable BlockState p_371320_, @Nullable Direction p_371369_, @NotNull RandomSource p_371947_, @NotNull ModelData modelData, @Nullable RenderType renderType)
        {
            //called by block render
            List<BakedQuad> quads = super.getQuads(p_371320_, p_371369_, p_371947_, modelData, renderType);
            return SCALE_AND_MOVE(quads, x, y, z, scale);
        }

        @Override
        public void applyTransform(@NotNull ItemDisplayContext transformType, @NotNull PoseStack poseStack, boolean applyLeftHandTransform)
        {
            super.applyTransform(transformType, poseStack, applyLeftHandTransform);
            poseStack.translate(0.5 - scale / 2f, 0.5 - scale / 2f, 0.5 - scale / 2f);
        }

        public static List<BakedQuad> SCALE_AND_MOVE(Collection<BakedQuad> quads, float x, float y, float z, float scale)
        {
            ArrayList<BakedQuad> list = new ArrayList<>(quads.size());
            for (BakedQuad quad : quads)
            {
                int[] vertices = quad.getVertices().clone();
                for (int i = 0; i < 4; i++)
                {
                    int index = i * 8;

                    float sx = Float.intBitsToFloat(vertices[index]);
                    float sy = Float.intBitsToFloat(vertices[index + 1]);
                    float sz = Float.intBitsToFloat(vertices[index + 2]);

                    sx = sx * scale + x;
                    sy = sy * scale + y;
                    sz = sz * scale + z;

                    vertices[index] = Float.floatToRawIntBits(sx);
                    vertices[index + 1] = Float.floatToRawIntBits(sy);
                    vertices[index + 2] = Float.floatToRawIntBits(sz);
                }
                list.add(new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade(), quad.getLightEmission(), quad.hasAmbientOcclusion()));
            }

            return list;
        }
    }
}
