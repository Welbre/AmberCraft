package welbre.ambercraft.subblock.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.subblock.SubBlockBE;
import welbre.ambercraft.subblock.TinyBlockState;

import java.util.ArrayList;
import java.util.List;

public class SubBlockBakedModel implements IDynamicBakedModel
{

    public SubBlockBakedModel()
    {
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(
            @Nullable BlockState state,
            @Nullable Direction side,
            @NotNull RandomSource rand,
            @NotNull ModelData extraData,
            @Nullable RenderType renderType)
    {
        List<TinyBlockState> dat = extraData.get(SubBlockBE.TINY_BLOCK_STATE_MODEL_PROPERTY);
        ArrayList<BakedQuad> quads = new ArrayList<>();

        if (dat == null) return quads;


        for (TinyBlockState blockState : dat)
        {
            BakedModel model = blockState.getDefinition().staticModel(blockState);

            if (side != null)//requiring non-occluded facing
                quads.addAll(model.getQuads(state, side, rand, extraData, renderType));
            else //minecraft requiring all faces outside the occlusion, so we should return faces occluded by the block, but that should be render
                for (var set : blockState.fullOccluded.entrySet())
                    if (set.getValue() == null && !blockState.externalContact.contains(set.getKey()))//if the block is occluded by some other block in the side AND the state isn't side by side
                        quads.addAll(model.getQuads(state, set.getKey(), rand, extraData, renderType));
        }

        return quads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ResourceLocation.withDefaultNamespace("blocks/air"));
    }

    @Override
    public @NotNull ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }
}
