package welbre.ambercraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import welbre.ambercraft.Main;
import welbre.ambercraft.client.item.CableSpecialRender;
import welbre.ambercraft.client.models.CentredCableModelLoader;
import welbre.ambercraft.client.models.FacedCableBakedModel;
import welbre.ambercraft.client.models.FacedCableModelLoader;
import welbre.ambercraft.client.BER.HeatSinkBER;
import welbre.ambercraft.datagen.template.CentredCableLoaderBuilder;
import welbre.ambercraft.datagen.template.FacedCableLoaderBuilder;

import static welbre.ambercraft.Main.MOD_ID;
import static welbre.ambercraft.Main.Tiles.HEAT_SINK_BLOCK_ENTITY;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientMain {
    public static BakedModel HEAT_SINK_MODEL;

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(HEAT_SINK_BLOCK_ENTITY.get(), HeatSinkBER::new);
    }

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        // Add your model's ResourceLocation so the game knows to load its JSON
        event.register(ResourceLocation.fromNamespaceAndPath(MOD_ID, "block/heat_sink"));
    }

    @SubscribeEvent
    public static void onModelBakingCompleted(ModelEvent.BakingCompleted event) {
        ModelManager modelManager = Minecraft.getInstance().getModelManager();
        HEAT_SINK_MODEL = modelManager.getStandaloneModel(ResourceLocation.parse("ambercraft:block/heat_sink"));
    }

    @SubscribeEvent
    public static void modelInit(ModelEvent.RegisterLoaders event) {
        event.register(CentredCableLoaderBuilder.ID, new CentredCableModelLoader());
        event.register(FacedCableLoaderBuilder.ID, new FacedCableModelLoader());
    }

    @SubscribeEvent
    public static void specialModels(RegisterSpecialModelRendererEvent event){
        event.register(ResourceLocation.fromNamespaceAndPath(MOD_ID,"cable_special_render"), CableSpecialRender.UnBacked.CODEC);
    }
}
