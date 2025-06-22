package welbre.ambercraft.client.models;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.blockentity.FacedCableBlockEntity;
import welbre.ambercraft.blocks.FacedCableBlock;
import welbre.ambercraft.blocks.HeatConductorBlock;
import welbre.ambercraft.client.RenderHelper;

import java.util.ArrayList;
import java.util.List;

import static welbre.ambercraft.client.RenderHelper.FROM_AABB;

public class FacedCableBakedModel implements IDynamicBakedModel {
    private final ModelBaker baker;
    private final ItemTransforms transforms;
    private final TextureSlots slots;
    private TextureAtlasSprite sprite;

    public FacedCableBakedModel(TextureSlots textureSlots, ModelBaker baker, ModelState modelState, ItemTransforms transforms) {
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
        ArrayList<BakedQuad> quads = new ArrayList<>();
        QuadBakingVertexConsumer consumer = new QuadBakingVertexConsumer();
        consumer.setSprite(sprite);

        float o = 0.25f;

        //render the item
        if (state == null)
            return new ArrayList<>(RenderHelper.CUBE_CENTRED(consumer, sprite, 0.3f));
        if (side != null)
            return List.of();
        Integer mask = extraData.get(FacedCableBlockEntity.CONNECTION_MASK_PROPERTY);
        if (mask == null)
        {
            return List.of();
        }
        int down = mask & 31;
        if (down != 0)
        {
            AABB c = AABB.ofSize(new Vec3(.5, o/4f, .5), o, o/2f, o);//center
            quads.addAll(RenderHelper.FROM_AABB(consumer,sprite, c));

            if ((down & 1) != 0)//up
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, 0, c.maxX, c.maxY, c.minZ).bounds()));
            if ((down & 2) != 0)//left
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(0, c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds()));
            if ((down & 4) != 0)//down
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, c.maxZ, c.maxX, c.maxY, 1f).bounds()));
            if ((down & 8) != 0)//right
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.maxX, c.minY, c.minZ, 1f, c.maxY, c.maxZ).bounds()));
        }
        int up = mask & (31 << 5);
        if (up != 0)
        {
            AABB c = AABB.ofSize(new Vec3(.5, 1-o/4f, .5), o, o/2f, o);//center
            quads.addAll(FROM_AABB(consumer, sprite, c));
            if ((up & (1<<5)) != 0)//up
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, c.maxZ, c.maxX, c.maxY, 1f).bounds()));
            if ((up & (2<<5)) != 0)//left
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(0, c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds()));
            if ((up & (4<<5)) != 0)//down
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, 0f, c.maxX, c.maxY, c.minZ).bounds()));
            if ((up & (8<<5)) != 0)//right
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.maxX, c.minY, c.minZ, 1f, c.maxY, c.maxZ).bounds()));
        }
        int north = mask & (31 << 10);
        if (north != 0)
        {
            AABB c = AABB.ofSize(new Vec3(.5, .5, o/4f), o, o, o/2f);//center
            quads.addAll(FROM_AABB(consumer,sprite, c));
            if ((north & (1<<10)) != 0)//up
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.maxY, c.minZ, c.maxX, 1f, c.maxZ).bounds()));
            if ((north & (2<<10)) != 0)//left
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(0, c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds()));
            if ((north & (4<<10)) != 0)//down
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, 0, c.minZ, c.maxX, c.minY, c.maxZ).bounds()));
            if ((north & (8<<10)) != 0)//right
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.maxX, c.minY, c.minZ, 1f, c.maxY, c.maxZ).bounds()));
        }
        int south = mask & (31 << 15);
        if (south != 0)
        {
            AABB c = AABB.ofSize(new Vec3(.5, .5, 1-o/4f), o, o, o/2f);//center
            quads.addAll(FROM_AABB(consumer,sprite, c));
            if ((south & (1<<15)) != 0)//up
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.maxY, c.minZ, c.maxX, 1f, c.maxZ).bounds()));
            if ((south & (2<<15)) != 0)//left
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.maxX, c.minY, c.minZ, 1f, c.maxY, c.maxZ).bounds()));
            if ((south & (4<<15)) != 0)//down
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, 0, c.minZ, c.maxX, c.minY, c.maxZ).bounds()));
            if ((south & (8<<15)) != 0)//right
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(0, c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds()));
        }
        int west = mask & (31 << 20);
        if (west != 0)
        {
            AABB c = AABB.ofSize(new Vec3(o/4f, .5, 0.5),o/2f,o,o);//center
            quads.addAll(FROM_AABB(consumer,sprite, c));
            if ((west & (1<<20)) != 0)//up
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.maxY, c.minZ, c.maxX, 1f, c.maxZ).bounds()));
            if ((west & (2<<20)) != 0)//left
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, c.maxZ, c.maxX, c.maxY, 1f).bounds()));
            if ((west & (4<<20)) != 0)//down
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, 0, c.minZ, c.maxX, c.minY, c.maxZ).bounds()));
            if ((west & (8<<20)) != 0)//right
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, 0, c.maxX, c.maxY, c.minZ).bounds()));
        }
        int east = mask & (31 << 25);
        if (east != 0)
        {
            AABB c = AABB.ofSize(new Vec3(1-o/4f, .5, 0.5),o/2f,o,o);//center
            quads.addAll(FROM_AABB(consumer,sprite, c));
            if ((east & (1<<25)) != 0)//up
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.maxY, c.minZ, c.maxX, 1f, c.maxZ).bounds()));
            if ((east & (2<<25)) != 0)//left
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, 0, c.maxX, c.maxY, c.minZ).bounds()));
            if ((east & (4<<25)) != 0)//down
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, 0, c.minZ, c.maxX, c.minY, c.maxZ).bounds()));
            if ((east & (8<<25)) != 0)//right
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, c.maxZ, c.maxX, c.maxY, 1f).bounds()));
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
}
