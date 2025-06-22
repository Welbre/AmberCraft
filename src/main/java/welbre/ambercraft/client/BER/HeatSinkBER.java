package welbre.ambercraft.client.BER;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;
import welbre.ambercraft.blockentity.HeatSinkBlockEntity;
import welbre.ambercraft.client.ClientMain;

import java.util.List;

public record HeatSinkBER(
        BlockEntityRendererProvider.Context context) implements BlockEntityRenderer<HeatSinkBlockEntity> {

    @Override
    public void render(HeatSinkBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        double temperature = blockEntity.heatModule.getTemperature();
        float fade = Math.max((float) (((300 - (Math.max(300, temperature))) / 300f) + 1f), 0f);
        BlockState state = blockEntity.getBlockState();
        PoseStack.Pose pose = poseStack.last();

        for (RenderType rt : ClientMain.HEAT_SINK_MODEL.getRenderTypes(state, RandomSource.create(42), ModelData.EMPTY))
        {
            VertexConsumer buffer = bufferSource.getBuffer(RenderTypeHelper.getEntityRenderType(rt));
            List<BakedQuad> quads = ClientMain.HEAT_SINK_MODEL.getQuads(state, null, RandomSource.create(42), ModelData.EMPTY, rt);

            for (BakedQuad quad : quads)
            {
                buffer.putBulkData(
                        pose,
                        quad,
                        1f,
                        fade,
                        fade,
                        1f,
                        packedLight,
                        packedOverlay);
            }
        }
    }
}
