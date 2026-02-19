package welbre.ambercraft.subblock.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.subblock.TinyItemDataComponent;

import java.util.List;

public class TinyItemModel implements ItemModel
{
    @Override
    public void update(
            @NotNull ItemStackRenderState renderState,
            @NotNull ItemStack stack,
            @NotNull ItemModelResolver itemModelResolver,
            @NotNull ItemDisplayContext displayContext,
            @Nullable ClientLevel level,
            @Nullable LivingEntity entity,
            int seed)
    {
        TinyItemDataComponent component = stack.get(AmberCraft.DataComponents.TINY_BLOCK_DATA_COMPONENT.get());
        if (component == null)
            return;
        List<BakedQuad> bakedQuads = component.get().staticRender(null, null, RandomSource.create(), ModelData.EMPTY, RenderType.SOLID);

        renderState.clear();
        ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
        var model = new BakedModel() {
            @Override
            public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
                return bakedQuads;
            }

            @Override
            public boolean useAmbientOcclusion() {
                return true;
            }

            @Override
            public boolean isGui3d() {
                return true;
            }

            @Override
            public boolean usesBlockLight() {
                return true;
            }

            @Override
            public TextureAtlasSprite getParticleIcon() {
                return bakedQuads.get(0).getSprite();
            }

            @Override
            public ItemTransforms getTransforms() {
                return ItemTransforms.NO_TRANSFORMS;
            }
        };

        layer.setupBlockModel(model, Sheets.solidBlockSheet());
    }

    public record Unbaked() implements ItemModel.Unbaked
    {
        public static final MapCodec<TinyItemModel.Unbaked> MAP_CODEC = MapCodec.unit(TinyItemModel.Unbaked::new);

        @Override
        public @NotNull MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public @NotNull ItemModel bake(@NotNull BakingContext context) {
            return new TinyItemModel();
        }

        @Override
        public void resolveDependencies(@NotNull Resolver resolver)
        {
        }
    }
}
