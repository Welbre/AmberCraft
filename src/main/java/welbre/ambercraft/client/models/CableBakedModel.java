package welbre.ambercraft.client.models;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
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
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.blocks.HeatConductorBlock;

import static welbre.ambercraft.client.RenderHelper.*;

import java.util.ArrayList;
import java.util.List;

public class CableBakedModel implements IDynamicBakedModel {
    ModelBaker baker;
    ItemTransforms transforms;
    TextureSlots textureSlots;

    public CableBakedModel(ModelBaker baker, ItemTransforms transforms, TextureSlots textureSlots) {
        this.baker = baker;
        this.transforms = transforms;
        this.textureSlots = textureSlots;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {

        QuadBakingVertexConsumer consumer = new QuadBakingVertexConsumer();
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ResourceLocation.parse("ambercraft:block/copper_heat_conductor"));
        consumer.setSprite(sprite);

        float o = HeatConductorBlock.MODEL_SIZE;
        ArrayList<BakedQuad> quads = new ArrayList<>(CUBE_CENTRED(consumer, sprite, o));

        if (state != null)
        {
            AABB c = AABB.ofSize(new Vec3(.5, .5f, .5), o, o, o);//center
            if (state.getValue(HeatConductorBlock.UP))
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.maxY, c.minZ, c.maxX, 1f, c.maxZ).bounds()));
            if (state.getValue(HeatConductorBlock.DOWN))
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, 0, c.minZ, c.maxX, c.minY, c.maxZ).bounds()));
            if (state.getValue(HeatConductorBlock.NORTH))
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, 0, c.maxX, c.maxY, c.minZ).bounds()));
            if (state.getValue(HeatConductorBlock.SOUTH))
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, c.maxZ, c.maxX, c.maxY, 1f).bounds()));
            if (state.getValue(HeatConductorBlock.WEST))
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(0, c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds()));
            if (state.getValue(HeatConductorBlock.EAST))
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.maxX, c.minY, c.minZ, 1f, c.maxY, c.maxZ).bounds()));
        }

        return quads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ResourceLocation.parse("ambercraft:block/copper_heat_conductor"));
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
