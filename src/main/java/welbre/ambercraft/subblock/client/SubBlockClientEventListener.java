package welbre.ambercraft.subblock.client;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import welbre.ambercraft.AmberCraft;

import static welbre.ambercraft.AmberCraft.MOD_ID;

/// Client Events related to the SubBlock System.
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = MOD_ID, value = Dist.CLIENT)
public final class SubBlockClientEventListener
{
    private SubBlockClientEventListener() {
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
