package welbre.ambercraft.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PayLoadRegister {
    public static void registerPayLoads(RegisterPayloadHandlersEvent event)
    {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                NetworkViewerPayLoad.TYPE,
                NetworkViewerPayLoad.STREAM_CODEC,
                NetworkViewerPayLoad::handleOnClient
        );
    }
}
