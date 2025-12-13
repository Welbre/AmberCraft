package welbre.ambercraft.client.models;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.blockentity.FacedCableBE;
import welbre.ambercraft.cables.CableState;
import welbre.ambercraft.cables.FaceState;
import welbre.ambercraft.cables.FaceState.Connection;
import welbre.ambercraft.client.RenderHelper;

import java.util.ArrayList;
import java.util.List;

import static welbre.ambercraft.client.RenderHelper.FROM_AABB;

public class FacedCableBakedModel implements IDynamicBakedModel {
    private final ItemTransforms transforms;

    public FacedCableBakedModel(ItemTransforms transforms) {
        this.transforms = transforms;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
        QuadBakingVertexConsumer consumer = new QuadBakingVertexConsumer();

        if (side != null)
            return List.of();
        CableState status = extraData.get(FacedCableBE.CONNECTION_MASK_PROPERTY);
        if (status == null)
            return List.of();

        //render the item
        if (state == null)
        {
            FaceState faceState = status.getFaceStatus(Direction.DOWN);
            int color = faceState.data.color;
            var sprite = faceState.type.getInsulationMaterial().sprite();
            final double[] s = new double[]{faceState.type.getWidth(), faceState.type.getHeight()};
            return new ArrayList<>(RenderHelper.FROM_AABB(consumer,sprite,AABB.ofSize(new Vec3(.5,.5,.5),s[0],s[1],s[0]), color));
        }

        return FacedCableModelHelper.GET_QUAD_FROM_STATE(status);
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
    public ItemTransforms getTransforms() {
        return this.transforms;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return ItemBlockRenderTypes.getRenderLayers(state);
    }
}
