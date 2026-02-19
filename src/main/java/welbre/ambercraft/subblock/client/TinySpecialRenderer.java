package welbre.ambercraft.subblock.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.special.ShieldSpecialRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.subblock.TinyItemDataComponent;

public record TinySpecialRenderer() implements SpecialModelRenderer<String>
{
    @Override
    public void render(
            @Nullable String patterns,
            @NotNull ItemDisplayContext displayContext,
            @NotNull PoseStack poseStack,
            @NotNull MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay,
            boolean hasFoilType)
    {
        System.out.println("Is trying to render the item!");
        //this is dynamic rendering!
    }

    @Override
    public @NotNull String extractArgument(@NotNull ItemStack stack)
    {
        TinyItemDataComponent data = stack.get(AmberCraft.DataComponents.TINY_BLOCK_DATA_COMPONENT.get());
        if (data == null)
            return "";
        else
            return data.tinyBlockKey();
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked
    {
        public static final MapCodec<TinySpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(TinySpecialRenderer.Unbaked::new);

        @Override
        public @NotNull SpecialModelRenderer<?> bake(@NotNull EntityModelSet modelSet)
        {
            return new TinySpecialRenderer();
        }

        @Override
        public @NotNull MapCodec<? extends SpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
