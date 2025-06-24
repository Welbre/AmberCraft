package welbre.ambercraft.client.item;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.Main;
import welbre.ambercraft.blockentity.FacedCableBlockEntity;
import welbre.ambercraft.cables.CableStatus;
import welbre.ambercraft.cables.FaceStatus;
import welbre.ambercraft.client.models.FacedCableBakedModel;

public record CableSpecialRender() implements SpecialModelRenderer<Integer> {
    @Override
    public void render(@Nullable Integer color, ItemDisplayContext displayContext, PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay, boolean hasFoilType) {
        BakedModel model = new FacedCableBakedModel(
                new Material(
                        TextureAtlas.LOCATION_BLOCKS,
                        ResourceLocation.parse("minecraft:block/white_wool")
                ),
                null
        );
        CableStatus status = new CableStatus();

        color = color == null ? -1 : color;
        status.addCenter(Direction.DOWN, color,0);

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.SOLID);
        var pose = poseStack.last();
        for (BakedQuad quad : model.getQuads(null, null, RandomSource.create(42), ModelData.of(FacedCableBlockEntity.CONNECTION_MASK_PROPERTY, status), RenderType.SOLID))
            buffer.putBulkData(pose,quad,1f,1f,1f,1f,packedLight,packedOverlay,true);
    }

    @Override
    public @NotNull Integer extractArgument(ItemStack stack) {
        return stack.getComponents().get(Main.Components.CABLE_DATA_COMPONENT.get()).color();
    }

    public record UnBacked(ResourceLocation texture) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<CableSpecialRender.UnBacked> CODEC = ResourceLocation.CODEC.fieldOf("texture").xmap(UnBacked::new,CableSpecialRender.UnBacked::texture);

        @Override
        public @Nullable SpecialModelRenderer<?> bake(EntityModelSet modelSet) {
            return new CableSpecialRender();
        }

        @Override
        public MapCodec<? extends Unbaked> type() {
            return CODEC;
        }
    }
}
