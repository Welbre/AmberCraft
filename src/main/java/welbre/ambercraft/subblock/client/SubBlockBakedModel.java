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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.client.RenderHelper;
import welbre.ambercraft.client.models.FacedCableModelHelper;
import welbre.ambercraft.subblock.SubBlockBE;
import welbre.ambercraft.subblock.TinyBlockRegister;
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
        //todo, o valo que está sendo passado é baseado no subBlock, então, se um bloco há um bloco a direita
        //o jogo não requer o lado direito dos tinyBLock, o que causa umas das faces ficar invisível
        //precisar ser calculado dentro do próprio bloco, se há faces obstruídas pelos blocos vizinhos, OU
        //se os diferentes tinyBlockState estão se tocando, fazendo assim não necessário renderizar uma das suas faces

        for (TinyBlockState blockState : dat)
            //quads.addAll(blockState.definition.staticRender(blockState, side, rand, extraData, renderType));
        {
            BakedModel model = blockState.definition.staticModel(blockState);
            quads.addAll(model.getQuads(state,side,rand,extraData,renderType));
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
