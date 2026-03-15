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
    public static boolean SHOW_DEBUG_BOX = true;

    @Override
    public void render(
            @NotNull SubBlockBE be,
            float partialTick,
            @NotNull PoseStack poseStack,
            @NotNull MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay)
    {
        if (SHOW_DEBUG_BOX)
        {
            for (var tb : be.tinyBS)
            {
                ShapeRenderer.renderShape(
                        poseStack,
                        bufferSource.getBuffer(RenderType.lines()),
                        tb.getTranslatedShape(),
                        0,
                        0,
                        0,
                        tb instanceof SharedTinyBlockState ?  ARGB.color(102, 0xcc, 0, 0) : ARGB.color(102, 64, 200, 200)
                );
            }

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
}
