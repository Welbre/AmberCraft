package welbre.ambercraft.subblock.layz;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.client.RenderHelper;
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
    public List<BakedQuad> staticRender(
            @NotNull TinyBlockState state,
            @NotNull Direction cull,
            @NotNull RandomSource rand,
            @NotNull ModelData extraData,
            @Nullable RenderType renderType
    )
    {
        var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(block.defaultBlockState());
        List<BakedQuad> quads = model.getQuads(block.defaultBlockState(), cull, rand, extraData, renderType);

        //each 1 unity is a 1/16 of a block, so we need to convert de 16 scale in the subBlock to the 1 scale in the block
        return SCALE_AND_MOVE(quads, state.x / 16f, state.y / 16f, state.z / 16f, size / 16f);
    }

    @Override
    public void dynamicRender(TinyBlockState state)
    {

    }

    private static List<BakedQuad> SCALE_AND_MOVE(Collection<BakedQuad> quads, float x, float y, float z, float scale)
    {
        ArrayList<BakedQuad> newone = new ArrayList<>(quads.size());
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
            newone.add(new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade(), quad.getLightEmission(), quad.hasAmbientOcclusion()));
        }

        return newone;
    }
}
