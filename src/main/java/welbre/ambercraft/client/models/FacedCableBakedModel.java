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
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
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
import welbre.ambercraft.cables.CableDataComponent;
import welbre.ambercraft.cables.CableStatus;
import welbre.ambercraft.cables.FaceStatus;
import welbre.ambercraft.cables.FaceStatus.Connection;
import welbre.ambercraft.client.RenderHelper;

import java.util.ArrayList;
import java.util.List;

import static welbre.ambercraft.client.RenderHelper.FROM_AABB;

public class FacedCableBakedModel implements IDynamicBakedModel {
    private final ItemTransforms transforms;
    private TextureAtlasSprite sprite;
    private Material cable;

    public FacedCableBakedModel(Material cable, ItemTransforms transforms) {
        this.transforms = transforms;
        this.cable = cable;
    }

    private void initTextures(){
        if (cable == null) {
            sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ResourceLocation.parse("minecraft:block/missing"));
            return;
        }

        sprite = cable.sprite();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
        initTextures();
        ArrayList<BakedQuad> quads = new ArrayList<>();
        QuadBakingVertexConsumer consumer = new QuadBakingVertexConsumer();
        consumer.setSprite(sprite);

        if (side != null)
            return List.of();
        CableStatus status = extraData.get(FacedCableBlockEntity.CONNECTION_MASK_PROPERTY);
        if (status == null)
        {
            return List.of();
        }
        //render the item
        if (state == null)
        {
            int color = status.getFaceStatus(Direction.DOWN).color;
            float[] s = CableDataComponent.UNPACK_SIZE(status.getFaceStatus(Direction.DOWN).packed_size);
            return new ArrayList<>(RenderHelper.FROM_AABB(consumer,sprite,AABB.ofSize(new Vec3(.5,.5,.5),s[0],s[1],s[0]), color));
        }


        FaceStatus down = status.getFaceStatus(Direction.DOWN);
        if (down != null)
        {
            final int color = down.color;
            final float[] s = CableDataComponent.UNPACK_SIZE(status.getFaceStatus(Direction.DOWN).packed_size);
            AABB c = AABB.ofSize(new Vec3(.5, s[1]/2f, .5), s[0], s[1], s[0]);//center
            quads.addAll(RenderHelper.FROM_AABB(consumer,sprite, c,color));

            //up
            if (down.connection[0] == Connection.DIAGONAL)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, -s[1], c.maxX, c.maxY, c.minZ).bounds(),color));
            else if (down.connection[0] == Connection.EXTERNAl)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, 0, c.maxX, c.maxY, c.minZ).bounds(),color));
            else if (down.connection[0] == Connection.INTERNAL)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, s[1], c.maxX, c.maxY, c.minZ).bounds(),color));
            //left
            if (down.connection[1] == Connection.DIAGONAL)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(-s[1], c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds(),color));
            else if (down.connection[1] == Connection.EXTERNAl)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(0, c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds(),color));
            else if (down.connection[1] == Connection.INTERNAL)
                    quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(s[1], c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds(),color));
            //down
            if (down.connection[2] == Connection.DIAGONAL)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, c.maxZ, c.maxX, c.maxY, 1f+s[1]).bounds(),color));
            else if (down.connection[2] == Connection.EXTERNAl)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, c.maxZ, c.maxX, c.maxY, 1f).bounds(),color));
            else if (down.connection[2] == Connection.INTERNAL)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, c.maxZ, c.maxX, c.maxY, 1f-s[1]).bounds(),color));
            //right
            if (down.connection[3] == Connection.DIAGONAL)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.maxX, c.minY, c.minZ, 1f+s[1], c.maxY, c.maxZ).bounds(),color));
            else if (down.connection[3] == Connection.EXTERNAl)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.maxX, c.minY, c.minZ, 1f, c.maxY, c.maxZ).bounds(),color));
            else if (down.connection[3] == Connection.INTERNAL)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.maxX, c.minY, c.minZ, 1f-s[1], c.maxY, c.maxZ).bounds(),color));
        }
        FaceStatus up = status.getFaceStatus(Direction.UP);
        if (up != null)
        {
            final int color = up.color;
            final float[] s = CableDataComponent.UNPACK_SIZE(status.getFaceStatus(Direction.UP).packed_size);
            AABB c = AABB.ofSize(new Vec3(.5, 1-s[1]/2f, .5), s[0], s[1], s[0]);//center
            quads.addAll(FROM_AABB(consumer, sprite, c, color));
            //up south
            if (up.connection[0] == Connection.DIAGONAL)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, c.maxZ, c.maxX, c.maxY, 1f+s[1]).bounds(),color));
            else if (up.connection[0] == Connection.EXTERNAl)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, c.maxZ, c.maxX, c.maxY, 1f).bounds(),color));
            else if (up.connection[0] == Connection.INTERNAL)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, c.maxZ, c.maxX, c.maxY, 1f-s[1]).bounds(),color));
            //left west
            if (up.connection[1] == Connection.DIAGONAL)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(-s[1], c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds(),color));
            else if (up.connection[1] == Connection.EXTERNAl)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(0, c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds(),color));
            else if (up.connection[1] == Connection.INTERNAL)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(s[1], c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds(),color));
            //down north
            if (up.connection[2] == Connection.DIAGONAL)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, -s[1], c.maxX, c.maxY, c.minZ).bounds(),color));
            else if (up.connection[2] == Connection.EXTERNAl)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, 0, c.maxX, c.maxY, c.minZ).bounds(),color));
            else if (up.connection[2] == Connection.INTERNAL)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, s[1], c.maxX, c.maxY, c.minZ).bounds(),color));
            //right east
            if (up.connection[3] == Connection.DIAGONAL)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.maxX, c.minY, c.minZ, 1f+s[1], c.maxY, c.maxZ).bounds(),color));
            else if (up.connection[3] == Connection.EXTERNAl)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.maxX, c.minY, c.minZ, 1f, c.maxY, c.maxZ).bounds(),color));
            else if (up.connection[3] == Connection.INTERNAL)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.maxX, c.minY, c.minZ, 1f-s[1], c.maxY, c.maxZ).bounds(),color));
        }
        FaceStatus north = status.getFaceStatus(Direction.NORTH);
        if (north != null)
        {
            final int color = north.color;
            final float[] s = CableDataComponent.UNPACK_SIZE(status.getFaceStatus(Direction.NORTH).packed_size);
            AABB c = AABB.ofSize(new Vec3(.5, .5, s[1]/2f), s[0], s[0], s[1]);//center
            quads.addAll(FROM_AABB(consumer,sprite, c, color));

            if (north.connection[0] != Connection.EMPTY)//up
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.maxY, c.minZ, c.maxX, 1f, c.maxZ).bounds(),color));
            //left
            if (north.connection[1] == Connection.EXTERNAl)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(0, c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds(),color));
            else if (north.connection[1] == Connection.DIAGONAL)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(-s[1], c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds(),color));
            else if (north.connection[1] == Connection.INTERNAL)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(s[1], c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds(),color));
            //down
            if (north.connection[2] != Connection.EMPTY)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, 0, c.minZ, c.maxX, c.minY, c.maxZ).bounds(),color));
            //right
            if (north.connection[3] == Connection.EXTERNAl)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.maxX, c.minY, c.minZ, 1f, c.maxY, c.maxZ).bounds(),color));
            else if (north.connection[3] == Connection.DIAGONAL)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.maxX, c.minY, c.minZ, 1f+s[1], c.maxY, c.maxZ).bounds(),color));
            else if (north.connection[3] == Connection.INTERNAL)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.maxX, c.minY, c.minZ, 1f-s[1], c.maxY, c.maxZ).bounds(),color));
        }
        FaceStatus south = status.getFaceStatus(Direction.SOUTH);
        if (south != null)
        {
            final int color = south.color;
            final float[] s = CableDataComponent.UNPACK_SIZE(status.getFaceStatus(Direction.SOUTH).packed_size);
            AABB c = AABB.ofSize(new Vec3(.5, .5, 1-s[1]/2f), s[0],s[0],s[1]);//center
            quads.addAll(FROM_AABB(consumer,sprite, c, color));
            if (south.connection[0] != Connection.EMPTY)//up
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.maxY, c.minZ, c.maxX, 1f, c.maxZ).bounds(),color));
            //left
            if (south.connection[1] == Connection.EXTERNAl)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.maxX, c.minY, c.minZ, 1f, c.maxY, c.maxZ).bounds(),color));
            else if (south.connection[1] == Connection.DIAGONAL)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.maxX, c.minY, c.minZ, 1f+s[1], c.maxY, c.maxZ).bounds(),color));
            else if (south.connection[1] == Connection.INTERNAL)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.maxX, c.minY, c.minZ, 1f-s[1], c.maxY, c.maxZ).bounds(),color));
            //down
            if (south.connection[2] != Connection.EMPTY)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, 0, c.minZ, c.maxX, c.minY, c.maxZ).bounds(),color));
            //right
            if (south.connection[3] == Connection.EXTERNAl)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(0, c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds(),color));
            else if (south.connection[3] == Connection.DIAGONAL)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(-s[1], c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds(),color));
            else if (south.connection[3] == Connection.INTERNAL)
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(s[1], c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds(),color));
        }
        FaceStatus west = status.getFaceStatus(Direction.WEST);
        if (west != null)
        {
            final int color = west.color;
            final float[] s = CableDataComponent.UNPACK_SIZE(status.getFaceStatus(Direction.WEST).packed_size);
            AABB c = AABB.ofSize(new Vec3(s[1]/2f, .5, 0.5), s[1],s[0],s[0]);//center
            quads.addAll(FROM_AABB(consumer,sprite, c, color));
            if (west.connection[0] != Connection.EMPTY)//up
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.maxY, c.minZ, c.maxX, 1f, c.maxZ).bounds(),color));
            if (west.connection[1] != Connection.EMPTY)//left
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, c.maxZ, c.maxX, c.maxY, 1f).bounds(),color));
            if (west.connection[2] != Connection.EMPTY)//down
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, 0, c.minZ, c.maxX, c.minY, c.maxZ).bounds(),color));
            if (west.connection[3] != Connection.EMPTY)//right
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, 0, c.maxX, c.maxY, c.minZ).bounds(),color));
        }
        FaceStatus east = status.getFaceStatus(Direction.EAST);
        if (east != null)
        {
            final int color = east.color;
            final float[] s = CableDataComponent.UNPACK_SIZE(status.getFaceStatus(Direction.EAST).packed_size);
            AABB c = AABB.ofSize(new Vec3(1-s[1]/2f, .5, 0.5),s[1],s[0],s[0]);//center
            quads.addAll(FROM_AABB(consumer,sprite, c, color));

            if (east.connection[0] != Connection.EMPTY)//up
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.maxY, c.minZ, c.maxX, 1f, c.maxZ).bounds(),color));
            if (east.connection[1] != Connection.EMPTY)//left
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, 0, c.maxX, c.maxY, c.minZ).bounds(),color));
            if (east.connection[2] != Connection.EMPTY)//down
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, 0, c.minZ, c.maxX, c.minY, c.maxZ).bounds(),color));
            if (east.connection[3] != Connection.EMPTY)//right
                quads.addAll(FROM_AABB(consumer, sprite, Shapes.box(c.minX, c.minY, c.maxZ, c.maxX, c.maxY, 1f).bounds(),color));
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
