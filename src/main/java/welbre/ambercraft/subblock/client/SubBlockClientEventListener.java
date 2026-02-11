package welbre.ambercraft.subblock.client;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import welbre.ambercraft.AmberCraft;

/// Client Events related to the SubBlock System.
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = AmberCraft.MOD_ID, value = Dist.CLIENT)
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
}
