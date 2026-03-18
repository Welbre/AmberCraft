package welbre.ambercraft.subblock.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import welbre.ambercraft.AmberCraft;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = AmberCraft.MOD_ID)
public class PayloadRegister
{

    @SubscribeEvent
    public static void registerPayLoads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(SubBlockStartBreakingState.TYPE,SubBlockStartBreakingState.STREAM_CODEC, SubBlockStartBreakingState::handleOnServer);
    }
}
