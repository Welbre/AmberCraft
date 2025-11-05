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

    @Override
    public void render(@NotNull InsulatorBE blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay)
    {
        if (blockEntity.getCablePos() == null)
            return;

        PoseStack.Pose last = poseStack.last();

        Vec3 v0 = blockEntity.getBlockPos().getCenter();

        for (BlockPos cablePos : blockEntity.getCablePos())
        {
            Vector3f end = cablePos.getCenter().subtract(v0).add(0.5, 0.4, 0.5).toVector3f();

            VertexConsumer consumer = bufferSource.getBuffer(RenderType.debugLineStrip(200));
            consumer.addVertex(last, new Vector3f(.5f,.4f,.5f)).setColor(0,1,0,1);
            consumer.addVertex(last, end).setColor(0,1,0,1);
        }
    }

    @Override
    public int getViewDistance() {
        return BlockEntityRenderer.super.getViewDistance() * 10;
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull InsulatorBE blockEntity)
    {
        return blockEntity.getCablePos() != null;
    }
}
