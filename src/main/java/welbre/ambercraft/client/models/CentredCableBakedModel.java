package welbre.ambercraft.client.models;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
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
import welbre.ambercraft.blocks.HeatConductorBlock;

import static welbre.ambercraft.client.RenderHelper.*;

import java.util.ArrayList;
import java.util.List;

public class CentredCableBakedModel implements IDynamicBakedModel {
    ModelBaker baker;
    ItemTransforms transforms;
    TextureAtlasSprite sprite;
    TextureSlots slots;

    public CentredCableBakedModel(ModelBaker baker, ItemTransforms transforms, TextureSlots textureSlots) {
        this.baker = baker;
        this.transforms = transforms;
        this.slots = textureSlots;
    }

    private void initTextures(){
        Material cable = slots.getMaterial("cable");

        if (cable == null) {
            sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ResourceLocation.parse("minecraft:block/missing"));
            return;
        }
        if (sprite != null)
            return;

        sprite = cable.sprite();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
        initTextures();
        float radius;
        if (state == null) radius = 0.4f;
        else
        {
            radius = extraData.get(HeatConductorBlock.RADIUS_PROPERTY);
        }
        QuadBakingVertexConsumer consumer = new QuadBakingVertexConsumer();
        consumer.setSprite(sprite);

        ArrayList<BakedQuad> quads = new ArrayList<>(CUBE_CENTRED(consumer, sprite, radius));

        if (state != null)
        {
            AABB c = AABB.ofSize(new Vec3(.5, .5f, .5), radius, radius, radius);//center
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
        return sprite;
    }

    @Override
    public ItemTransforms getTransforms() {
        return this.transforms;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return ItemBlockRenderTypes.getRenderLayers(state);
    }

    @Override
    public @NotNull ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, @NotNull ModelData modelData) {
        if (state.getBlock() instanceof HeatConductorBlock conductor)
        {
            return modelData.derive().with(HeatConductorBlock.RADIUS_PROPERTY, conductor.model_radius).build();
        }
        return IDynamicBakedModel.super.getModelData(level, pos, state, modelData);
    }
}
