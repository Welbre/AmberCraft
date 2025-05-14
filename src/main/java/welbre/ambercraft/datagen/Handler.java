package welbre.ambercraft.datagen;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import welbre.ambercraft.Main;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = Main.MOD_ID)
public class Handler {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        var generator = event.getGenerator();
        var out = generator.getPackOutput();
        var provider = event.getLookupProvider();

        generator.addProvider(true, new AmberLanguageProvider(out));
        generator.addProvider(true, new AmberModelProvider(out));
        //generator.addProvider(true, new AmberModelProvider(out));
    }

    @SubscribeEvent
    public static void registerAdditional(ModelEvent.RegisterAdditional event) {
        event.register(ResourceLocation.fromNamespaceAndPath("examplemod", "block/example_unused_model"));
    }
}
