package welbre.ambercraft.subblock.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.subblock.TinyBlock;
import welbre.ambercraft.subblock.TinyBlockState;
import welbre.ambercraft.subblock.TinyItem;
import welbre.ambercraft.subblock.TinyItemDataComponent;

import java.util.ArrayList;

import static welbre.ambercraft.AmberCraft.MOD_ID;

/// Client Events related to the SubBlock System.
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = MOD_ID, value = Dist.CLIENT)
public final class SubBlockClientEventListener
{
    private SubBlockClientEventListener() {
    }

    @EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = MOD_ID, value = Dist.CLIENT)
    public static final class ForgeBus
    {
        /**
         * Renders a transparent version of the model in the world "a preview".<br>
         */
        @SubscribeEvent
        public static void onRenderHighLight(RenderHighlightEvent.Block event)
        {
            if (event.getCamera().getEntity() instanceof Player player)
            {
                ClientLevel level = Minecraft.getInstance().level;
                if (level == null)
                    return;

                ItemStack stack = player.getMainHandItem();
                if (stack.getItem() != AmberCraft.Items.TINY_ITEM.get())
                    stack = player.getOffhandItem();
                if (stack.getItem() != AmberCraft.Items.TINY_ITEM.get())
                    return;

                TinyItemDataComponent component = stack.get(AmberCraft.DataComponents.TINY_BLOCK_DATA_COMPONENT);
                if (component == null)
                    return;

                Vec3i vec = TinyItem.CONTEXT_TO_16_GRID(level, event.getTarget());
                System.out.println(event.getTarget().getLocation() + "\t" + vec.toString());

                if (!TinyItem.CAN_PLACE(component.get(), level, event.getTarget()))
                    return;

                {
                    BlockPos blockPos;
                    if (level.getBlockState(event.getTarget().getBlockPos()).is(AmberCraft.Blocks.SUB_BLOCK.get()))
                        blockPos = event.getTarget().getBlockPos();
                    else
                        blockPos = event.getTarget().getBlockPos().relative(event.getTarget().getDirection());

                    BlockState state = level.getBlockState(blockPos);

                    TinyBlock tinyBlock = component.get();
                    BakedModel bakedModel = tinyBlock.staticModel(new TinyBlockState(tinyBlock, vec.getX(), vec.getY(), vec.getZ()));

                    VertexConsumer buffer = event.getMultiBufferSource().getBuffer(RenderType.translucent());
                    RandomSource randomSource = level.getRandom();
                    ArrayList<BakedQuad> list = new ArrayList<>();

                    for (RenderType type : bakedModel.getRenderTypes(state, randomSource, ModelData.EMPTY))
                        for (Direction direction : Direction.values())
                            list.addAll(bakedModel.getQuads(state, direction, randomSource, ModelData.EMPTY, type));

                    PoseStack poseStack = new PoseStack();
                    poseStack.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                    poseStack.translate(new Vec3(0,0,0).subtract(event.getCamera().getPosition()));
                    for (BakedQuad quad : list)
                        buffer.putBulkData(poseStack.last(), quad, 1f, 1f, 1f, 0.25f, LevelRenderer.getLightColor(level, blockPos), OverlayTexture.NO_OVERLAY);
                }
            }
        }
    }

    /**
     * Used to overwrite a data-generated model to simply use the {@link SubBlockBakedModel} .
     */
    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult event)
    {
        event.getBakingResult().blockStateModels().put(new ModelResourceLocation(AmberCraft.Blocks.SUB_BLOCK.getId(),""), new SubBlockBakedModel());
    }

    @SubscribeEvent
    public static void registerSpecialModelRendererEvent(RegisterSpecialModelRendererEvent event)
    {
        event.register(ResourceLocation.fromNamespaceAndPath(MOD_ID,"tiny_item"), TinySpecialRenderer.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerItemModels(RegisterItemModelsEvent event) {
        event.register(ResourceLocation.fromNamespaceAndPath(MOD_ID, "tiny_item"), TinyItemModel.Unbaked.MAP_CODEC);
    }
}
