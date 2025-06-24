package welbre.ambercraft.client;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class RenderHelper {
    private RenderHelper(){}

    public static BakedQuad QUAD (
            QuadBakingVertexConsumer consumer,
            TextureAtlasSprite sprite,
            float x0, float y0, float z0, float u0, float v0, int c0,
            float x1, float y1, float z1, float u1, float v1, int c1,
            float x2, float y2, float z2, float u2, float v2, int c2,
            float x3, float y3, float z3, float u3, float v3, int c3
    ){
        consumer.setSprite(sprite);
        //consumer.setTintIndex(1);
        Vec3 n = new Vec3(x2,y2,z2).subtract(new Vec3(x1,y1,z1)).cross(new Vec3(x0,y0,z0).subtract(new Vec3(x1,y1,z1))).normalize();
        float nx = (float) n.x; float ny = (float) n.y; float nz = (float) n.z;
        consumer.setDirection(Direction.getApproximateNearest(n));
        consumer.setShade(true);

        consumer.addVertex(x0, y0, z0).setUv(sprite.getU(u0), sprite.getV(v0)).setColor(c0).setNormal(nx,ny,nz);
        consumer.addVertex(x1, y1, z1).setUv(sprite.getU(u1), sprite.getV(v1)).setColor(c1).setNormal(nx,ny,nz);
        consumer.addVertex(x2, y2, z2).setUv(sprite.getU(u2), sprite.getV(v2)).setColor(c2).setNormal(nx,ny,nz);
        consumer.addVertex(x3, y3, z3).setUv(sprite.getU(u3), sprite.getV(v3)).setColor(c3).setNormal(nx,ny,nz);

        return consumer.bakeQuad();
    }

    public static List<BakedQuad> CUBE_CENTRED(
            QuadBakingVertexConsumer consumer,
            TextureAtlasSprite sprite,
            float size
    ){
        return CUBE_CENTRED(consumer,sprite,size, new Vec3(0.5f, 0.5f, 0.5f));
    }
    public static List<BakedQuad> CUBE_CENTRED(
            QuadBakingVertexConsumer consumer,
            TextureAtlasSprite sprite,
            float size,
            int color_rgb
    ){
        return CUBE_CENTRED(consumer,sprite,size, new Vec3(0.5f, 0.5f, 0.5f), color_rgb);
    }

    public static List<BakedQuad> CUBE_CENTRED(
            QuadBakingVertexConsumer consumer,
            TextureAtlasSprite sprite,
            float size,
            Vec3 center
    ){
        return CUBE_CENTRED(consumer,sprite,size,center,0xffffff);
    }

    public static List<BakedQuad> CUBE_CENTRED(
            QuadBakingVertexConsumer consumer,
            TextureAtlasSprite sprite,
            float size,
            Vec3 center,
            int color_rgb
    ){
        float ax = (float) (center.x-size/2f);
        float bx = (float) (center.x+size/2f);
        float ay = (float) (center.y-size/2f);
        float by = (float) (center.y+size/2f);
        float az = (float) (center.z-size/2f);
        float bz = (float) (center.z+size/2f);

        return FROM_AABB(consumer, sprite, Shapes.box(ax,ay,az,bx,by,bz).bounds(), color_rgb);
    }

    public static List<BakedQuad> FROM_AABB(
            QuadBakingVertexConsumer consumer,
            TextureAtlasSprite sprite,
            AABB aabb
    ){
        return FROM_AABB(consumer,sprite,aabb,0xffffffff);
    }

    public static List<BakedQuad> FROM_AABB(
            QuadBakingVertexConsumer consumer,
            TextureAtlasSprite sprite,
            AABB aabb,
            int rgba
    ){
        List<BakedQuad> quads = new ArrayList<>();
        float ax = (float) aabb.minX;
        float bx = (float) aabb.maxX;
        float ay = (float) aabb.minY;
        float by = (float) aabb.maxY;
        float az = (float) aabb.minZ;
        float bz = (float) aabb.maxZ;

        //X+ face
        quads.add(QUAD(consumer, sprite,
                ax, by, az,0,0,rgba,
                ax, ay, az,0,1 ,rgba,
                ax, ay, bz,1,1,rgba,
                ax, by, bz,1,0,rgba
        ));
        //Z+ face
        quads.add(QUAD(consumer, sprite,
                bx, by, az,0,0,rgba,
                bx, ay, az,0,1,rgba,
                ax, ay, az,1,1,rgba,
                ax, by, az,1,0,rgba
        ));
        //Y+DOWN face
        quads.add(QUAD(consumer, sprite,
                ax, ay, bz,0,0,rgba,
                ax, ay, az,0,1,rgba,
                bx, ay, az,1,1,rgba,
                bx, ay, bz,1,0,rgba
        ));
        //X- face
        quads.add(QUAD(consumer, sprite,
                bx, by, bz,0,0,rgba,
                bx, ay, bz,0,1,rgba,
                bx, ay, az,1,1,rgba,
                bx, by, az,1,0,rgba
        ));
        //Z- face
        quads.add(QUAD(consumer, sprite,
                ax, by, bz,0,0,rgba,
                ax, ay, bz,0,1,rgba,
                bx, ay, bz,1,1,rgba,
                bx, by, bz,1,0,rgba
        ));
        //Y-UP face
        quads.add(QUAD(consumer, sprite,
                ax, by, az,0,0,rgba,
                ax, by, bz,0,1,rgba,
                bx, by, bz,1,1,rgba,
                bx, by, az,1,0,rgba
        ));

        return quads;
    }
}
