package welbre.ambercraft.subblock.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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

        if (dat == null || dat.isEmpty())
            return List.of();

        QuadBakingVertexConsumer consumer = new QuadBakingVertexConsumer();
        var sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ResourceLocation.parse("minecraft:block/stone"));
        consumer.setSprite(sprite);
        return RenderHelper.FROM_AABB(
                consumer,
                sprite,
                (dat.get(0)).definition.shape.bounds().move(dat.get(0).x/16f, dat.get(0).y/16f, dat.get(0).z/16f));
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
