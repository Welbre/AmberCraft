package welbre.ambercraft.client.BER;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import welbre.ambercraft.blockentity.electrical.InsulatorBE;

public record
InsulatorBER(BlockEntityRendererProvider.Context context) implements BlockEntityRenderer<InsulatorBE>
{
    /// Is the angular constant of a 2º equation that render the cable height.
    public static final float a = 0.002f;

    @Override
    public void render(@NotNull InsulatorBE blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay)
    {
        if (blockEntity.getCablePos() == null)
            return;

        PoseStack.Pose last = poseStack.last();

        Vec3 v0 = blockEntity.getBlockPos().getCenter();

        //render a cable for each cablePos in the blockEntity
        // the cablePos is the final point of the cable, where is another insulator connected to variable "blockEntity".
        for (BlockPos cablePos : blockEntity.getCablePos())
        {
            //this code work in a weird way, the poseStack already is set to go to the block position,
            //so set a vertex in the origin will set the vertex in the blockPos in the world.
            //therefore, the start is the block center, and the direction in a direction vector from the start to the cablePos
            //to render the cable shape as in the real life, we use a parabolic equation, specifically the a(i-k) -C = y
            //where a is a constant, k is the x vertex coordinate basically len / 2, and for last, the y is the height from the y vertex.
            //y isn't exactly the same y of the world!
            //"i" is a scalar value that we multiply in the direction to go from start to cablePos, when i = 0 we are in the start, and when i = len we are in the cablePos.
            //so the position of the point is start + direction*i

            Vector3f start = new Vector3f(.5f,.4f,.5f);
            Vector3f direction = cablePos.getCenter().toVector3f().sub(v0.toVector3f());
            float len = direction.length();
            //right here the length is stored, and the direction normalized to be used as a versor.
            direction.normalize();

            VertexConsumer consumer = bufferSource.getBuffer(RenderType.debugLineStrip(200));

            //the parabolic equation produces all cable coordinates,
            //but we must compute a constant C that when i = 0, and i = len, the y = 0.
            //but why ? remember when I say that adding a vertex in the origin(0,0,0) will produce a vertex in the block position?
            //so when y = 0, the element is rendered exactly in the cable position.
            //to compute this C is EXTREMELY simple, just apply the equation with i = 0,
            //this will result in the value that the equation assumes when i =0, so subtract it in the point, done!, when i = 0 the y will be zero, the same to i = len.
            float c =  (float) Math.pow(0 -(len / 2f), 2) * a;

            for (int i = 0 ; i <= len; i++)
            {
                //computer the parabolic equation with different i
                float y = (float) Math.pow(i - (len / 2f), 2) * a;
                //this uses the direction and the start point to find where between start and cablePos we are, basically a linear interpolation in a vector.
                //after finding where in the line that we are, add the y to simulate the parabolic shape in the Y axis and
                //subtract the constant C to set the i = 0 and i = len as the root, explained in the last comment.
                // start + direction*i + (0, parabolic shape, 0)
                consumer.addVertex(last, new Vector3f(direction).mul(i).add(start).add(0,y-c,0)).setColor(1,1,1,1);
            }

            //in case that len isn't integer, add a last vertex to complete the difference.
            if (Math.ceil(len) != len)
                consumer.addVertex(last, new Vector3f(direction).mul(len).add(start)).setColor(1, 1, 1, 1);
        }
    }

    @Override
    public int getViewDistance() {
        return 30 * 16; //32 chucks * 16 blocks, sodium max distance
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull InsulatorBE blockEntity)
    {
        return blockEntity.getCablePos() != null;
    }
}
