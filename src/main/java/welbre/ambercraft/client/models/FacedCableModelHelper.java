package welbre.ambercraft.client.models;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import welbre.ambercraft.cables.CableState;
import welbre.ambercraft.cables.FaceState;
import welbre.ambercraft.client.RenderHelper;

import java.util.ArrayList;
import java.util.List;

import static welbre.ambercraft.client.RenderHelper.FROM_AABB;

public final class FacedCableModelHelper
{
    public static List<BakedQuad> GET_QUAD_FROM_STATE(CableState state)
    {
        QuadBakingVertexConsumer consumer = new QuadBakingVertexConsumer();
        ArrayList<BakedQuad> quads = new ArrayList<>();

        FaceState down = state.getFaceStatus(Direction.DOWN);
        if (down != null)
        {
            final int color = down.data.color;
            var ins = down.type.getInsulationMaterial().sprite();
            var core = down.type.getCableMaterial().sprite();
            final double[] s = new double[]{down.type.getWidth(), down.type.getHeight()};
            AABB c = AABB.ofSize(new Vec3(.5, s[1]/2f, .5), s[0], s[1], s[0]);//center

            RENDER_CENTER(quads,consumer,Direction.DOWN,down,c,ins,core,color);
            //up
            if (down.connection[0] == FaceState.Connection.EXTERNAl)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.minX, c.minY, 0, c.maxX, c.maxY, c.minZ).bounds(),color));
            else if (down.connection[0] == FaceState.Connection.DIAGONAL)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.minX, c.minY, -s[1], c.maxX, c.maxY, c.minZ).bounds(),color));
            else if (down.connection[0] == FaceState.Connection.INTERNAL)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.minX, c.minY, s[1], c.maxX, c.maxY, c.minZ).bounds(),color));
            //left
            if (down.connection[1] == FaceState.Connection.EXTERNAl)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(0, c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds(),color));
            else if (down.connection[1] == FaceState.Connection.DIAGONAL)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(-s[1], c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds(),color));
            else if (down.connection[1] == FaceState.Connection.INTERNAL)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(s[1], c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds(),color));
            //down
            if (down.connection[2] == FaceState.Connection.EXTERNAl)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.minX, c.minY, c.maxZ, c.maxX, c.maxY, 1f).bounds(),color));
            else if (down.connection[2] == FaceState.Connection.DIAGONAL)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.minX, c.minY, c.maxZ, c.maxX, c.maxY, 1f+s[1]).bounds(),color));
            else if (down.connection[2] == FaceState.Connection.INTERNAL)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.minX, c.minY, c.maxZ, c.maxX, c.maxY, 1f-s[1]).bounds(),color));
            //right
            if (down.connection[3] == FaceState.Connection.DIAGONAL)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.maxX, c.minY, c.minZ, 1f+s[1], c.maxY, c.maxZ).bounds(),color));
            else if (down.connection[3] == FaceState.Connection.EXTERNAl)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.maxX, c.minY, c.minZ, 1f, c.maxY, c.maxZ).bounds(),color));
            else if (down.connection[3] == FaceState.Connection.INTERNAL)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.maxX, c.minY, c.minZ, 1f-s[1], c.maxY, c.maxZ).bounds(),color));
        }
        FaceState up = state.getFaceStatus(Direction.UP);
        if (up != null)
        {
            final int color = up.data.color;
            var ins = up.type.getInsulationMaterial().sprite();
            var core = up.type.getCableMaterial().sprite();
            final double[] s = new double[]{up.type.getWidth(), up.type.getHeight()};
            AABB c = AABB.ofSize(new Vec3(.5, 1-s[1]/2f, .5), s[0], s[1], s[0]);//center

            RENDER_CENTER(quads,consumer,Direction.UP,up,c,ins,core,color);
            //up south
            if (up.connection[0] == FaceState.Connection.EXTERNAl)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.minX, c.minY, c.maxZ, c.maxX, c.maxY, 1f).bounds(),color));
            else if (up.connection[0] == FaceState.Connection.DIAGONAL)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.minX, c.minY, c.maxZ, c.maxX, c.maxY, 1f+s[1]).bounds(),color));
            else if (up.connection[0] == FaceState.Connection.INTERNAL)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.minX, c.minY, c.maxZ, c.maxX, c.maxY, 1f-s[1]).bounds(),color));
            //left west
            if (up.connection[1] == FaceState.Connection.EXTERNAl)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(0, c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds(),color));
            else if (up.connection[1] == FaceState.Connection.DIAGONAL)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(-s[1], c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds(),color));
            else if (up.connection[1] == FaceState.Connection.INTERNAL)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(s[1], c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds(),color));
            //down north
            if (up.connection[2] == FaceState.Connection.EXTERNAl)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.minX, c.minY, 0, c.maxX, c.maxY, c.minZ).bounds(),color));
            else if (up.connection[2] == FaceState.Connection.DIAGONAL)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.minX, c.minY, -s[1], c.maxX, c.maxY, c.minZ).bounds(),color));
            else if (up.connection[2] == FaceState.Connection.INTERNAL)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.minX, c.minY, s[1], c.maxX, c.maxY, c.minZ).bounds(),color));
            //right east
            if (up.connection[3] == FaceState.Connection.EXTERNAl)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.maxX, c.minY, c.minZ, 1f, c.maxY, c.maxZ).bounds(),color));
            else if (up.connection[3] == FaceState.Connection.DIAGONAL)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.maxX, c.minY, c.minZ, 1f+s[1], c.maxY, c.maxZ).bounds(),color));
            else if (up.connection[3] == FaceState.Connection.INTERNAL)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.maxX, c.minY, c.minZ, 1f-s[1], c.maxY, c.maxZ).bounds(),color));
        }
        FaceState north = state.getFaceStatus(Direction.NORTH);
        if (north != null)
        {
            final int color = north.data.color;
            var ins = north.type.getInsulationMaterial().sprite();
            var core = north.type.getCableMaterial().sprite();
            final double[] s = new double[]{north.type.getWidth(), north.type.getHeight()};
            AABB c = AABB.ofSize(new Vec3(.5, .5, s[1]/2f), s[0], s[0], s[1]);//center

            RENDER_CENTER(quads,consumer,Direction.NORTH,north,c,ins,core,color);
            //up
            if (north.connection[0] != FaceState.Connection.EMPTY)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.minX, c.maxY, c.minZ, c.maxX, 1f, c.maxZ).bounds(),color));
            //left
            if (north.connection[1] == FaceState.Connection.EXTERNAl)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(0, c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds(),color));
            else if (north.connection[1] == FaceState.Connection.DIAGONAL)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(-s[1], c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds(),color));
            else if (north.connection[1] == FaceState.Connection.INTERNAL)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(s[1], c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds(),color));
            //down
            if (north.connection[2] != FaceState.Connection.EMPTY)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.minX, 0, c.minZ, c.maxX, c.minY, c.maxZ).bounds(),color));
            //right
            if (north.connection[3] == FaceState.Connection.EXTERNAl)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.maxX, c.minY, c.minZ, 1f, c.maxY, c.maxZ).bounds(),color));
            else if (north.connection[3] == FaceState.Connection.DIAGONAL)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.maxX, c.minY, c.minZ, 1f+s[1], c.maxY, c.maxZ).bounds(),color));
            else if (north.connection[3] == FaceState.Connection.INTERNAL)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.maxX, c.minY, c.minZ, 1f-s[1], c.maxY, c.maxZ).bounds(),color));
        }
        FaceState south = state.getFaceStatus(Direction.SOUTH);
        if (south != null)
        {
            final int color = south.data.color;
            var ins = south.type.getInsulationMaterial().sprite();
            var core = south.type.getCableMaterial().sprite();
            final double[] s = new double[]{south.type.getWidth(), south.type.getHeight()};
            AABB c = AABB.ofSize(new Vec3(.5, .5, 1-s[1]/2f), s[0],s[0],s[1]);//center

            RENDER_CENTER(quads,consumer,Direction.SOUTH,south,c,ins,core,color);
            if (south.connection[0] != FaceState.Connection.EMPTY)//up
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.minX, c.maxY, c.minZ, c.maxX, 1f, c.maxZ).bounds(),color));
            //left
            if (south.connection[1] == FaceState.Connection.EXTERNAl)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.maxX, c.minY, c.minZ, 1f, c.maxY, c.maxZ).bounds(),color));
            else if (south.connection[1] == FaceState.Connection.DIAGONAL)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.maxX, c.minY, c.minZ, 1f+s[1], c.maxY, c.maxZ).bounds(),color));
            else if (south.connection[1] == FaceState.Connection.INTERNAL)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.maxX, c.minY, c.minZ, 1f-s[1], c.maxY, c.maxZ).bounds(),color));
            //down
            if (south.connection[2] != FaceState.Connection.EMPTY)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.minX, 0, c.minZ, c.maxX, c.minY, c.maxZ).bounds(),color));
            //right
            if (south.connection[3] == FaceState.Connection.EXTERNAl)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(0, c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds(),color));
            else if (south.connection[3] == FaceState.Connection.DIAGONAL)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(-s[1], c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds(),color));
            else if (south.connection[3] == FaceState.Connection.INTERNAL)
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(s[1], c.minY, c.minZ, c.minX, c.maxY, c.maxZ).bounds(),color));
        }
        FaceState west = state.getFaceStatus(Direction.WEST);
        if (west != null)
        {
            final int color = west.data.color;
            var ins = west.type.getInsulationMaterial().sprite();
            var core = west.type.getCableMaterial().sprite();
            final double[] s = new double[]{west.type.getWidth(), west.type.getHeight()};
            AABB c = AABB.ofSize(new Vec3(s[1]/2f, .5, 0.5), s[1],s[0],s[0]);//center

            RENDER_CENTER(quads,consumer,Direction.WEST,west,c,ins,core,color);
            if (west.connection[0] != FaceState.Connection.EMPTY)//up
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.minX, c.maxY, c.minZ, c.maxX, 1f, c.maxZ).bounds(),color));
            if (west.connection[1] != FaceState.Connection.EMPTY)//left
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.minX, c.minY, c.maxZ, c.maxX, c.maxY, 1f).bounds(),color));
            if (west.connection[2] != FaceState.Connection.EMPTY)//down
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.minX, 0, c.minZ, c.maxX, c.minY, c.maxZ).bounds(),color));
            if (west.connection[3] != FaceState.Connection.EMPTY)//right
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.minX, c.minY, 0, c.maxX, c.maxY, c.minZ).bounds(),color));
        }
        FaceState east = state.getFaceStatus(Direction.EAST);
        if (east != null)
        {
            final int color = east.data.color;
            var ins = east.type.getInsulationMaterial().sprite();
            var core = east.type.getCableMaterial().sprite();
            final double[] s = new double[]{east.type.getWidth(), east.type.getHeight()};
            AABB c = AABB.ofSize(new Vec3(1-s[1]/2f, .5, 0.5),s[1],s[0],s[0]);//center

            RENDER_CENTER(quads,consumer,Direction.EAST,east,c,ins,core,color);
            if (east.connection[0] != FaceState.Connection.EMPTY)//up
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.minX, c.maxY, c.minZ, c.maxX, 1f, c.maxZ).bounds(),color));
            if (east.connection[1] != FaceState.Connection.EMPTY)//left
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.minX, c.minY, 0, c.maxX, c.maxY, c.minZ).bounds(),color));
            if (east.connection[2] != FaceState.Connection.EMPTY)//down
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.minX, 0, c.minZ, c.maxX, c.minY, c.maxZ).bounds(),color));
            if (east.connection[3] != FaceState.Connection.EMPTY)//right
                quads.addAll(FROM_AABB(consumer, ins, Shapes.box(c.minX, c.minY, c.maxZ, c.maxX, c.maxY, 1f).bounds(),color));
        }

        return quads;
    }

    public static void RENDER_CENTER(ArrayList<BakedQuad> quads, QuadBakingVertexConsumer consumer, Direction faceDir, FaceState state, AABB c, TextureAtlasSprite ins, TextureAtlasSprite core, int color) {
        Direction coreGlobal = null;
        for (Direction global : CableState.GET_FACE_DIRECTIONS(faceDir))
        {
            int index = CableState.getConnectionIndexByGlobalDir(faceDir, global);
            if (state.connection[index] != FaceState.Connection.EMPTY)
                if (coreGlobal == null)
                    coreGlobal = global;
                else
                {
                    coreGlobal = null;
                    break;
                }
        }

        if (coreGlobal != null)
        {
            Direction finalCoreGlobal = coreGlobal;
            quads.addAll(RenderHelper.FROM_AABB_SIDE_TEXTURED(consumer, dir -> dir == finalCoreGlobal ? core : ins
                    , c, dir -> dir == finalCoreGlobal ? 0xffffffff : color));
        }
        else
            quads.addAll(RenderHelper.FROM_AABB(consumer,ins, c,color));
    }
}
