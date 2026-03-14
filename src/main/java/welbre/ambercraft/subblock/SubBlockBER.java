package welbre.ambercraft.subblock;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.NotNull;

public record SubBlockBER(BlockEntityRendererProvider.Context context) implements BlockEntityRenderer<SubBlockBE>
{
    @Override
    public void render(
            @NotNull SubBlockBE be,
            float partialTick,
            @NotNull PoseStack poseStack,
            @NotNull MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay)
    {
        ShapeRenderer.renderShape(
                poseStack,
                bufferSource.getBuffer(RenderType.lines()),
                Shapes.block(),
                0,
                0,
                0,
                ARGB.color(102, -16777216)
        );
    }
}
