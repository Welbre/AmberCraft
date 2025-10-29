package welbre.ambercraft.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import welbre.ambercraft.network.facedcable.FacedCableRemoveFacePayload;
import welbre.ambercraft.network.facedcable.FacedCableStateChangePayload;

public class PayLoadRegister {
    public static void registerPayLoads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                NetworkViewerScreenPayLoad.TYPE,
                NetworkViewerScreenPayLoad.STREAM_CODEC,
                NetworkViewerScreenPayLoad::handleOnClient
        );

        registrar.playToServer(
                HeatSourceSetterPayload.TYPE,
                HeatSourceSetterPayload.STREAM_CODEC,
                HeatSourceSetterPayload::handleOnServer
        );

        registrar.playToClient(
                UpdateAmberSecureKeyPayload.TYPE,
                UpdateAmberSecureKeyPayload.STREAM_CODEC,
                UpdateAmberSecureKeyPayload::handleOnClient
        );

        registrar.playToServer(
                ModifyFieldsPayLoad.TYPE,
                ModifyFieldsPayLoad.STREAM_CODEC,
                ModifyFieldsPayLoad::handleOnServer
        );

        registrar.playToClient(
                AmberCraftScreenOpenerPayload.TYPE,
                AmberCraftScreenOpenerPayload.STREAM_CODEC,
                AmberCraftScreenOpenerPayload::handleOnClient
        );

        registrar.playToServer(
                VoltageSourceModifierPayload.TYPE,
                VoltageSourceModifierPayload.STREAM_CODEC,
                VoltageSourceModifierPayload::handleOnServer
        );

        registrar.playToServer(
                AmberValueModifierPayload.TYPE,
                AmberValueModifierPayload.STREAM_CODEC,
                AmberValueModifierPayload::handleOnServer
        );

        //faced cables payloads
        registrar.playToServer(FacedCableRemoveFacePayload.TYPE, FacedCableRemoveFacePayload.STREAM_CODEC, FacedCableRemoveFacePayload::handleOnServer);
        registrar.playToClient(FacedCableStateChangePayload.TYPE, FacedCableStateChangePayload.STREAM_CODEC, FacedCableStateChangePayload::handleOnClient);
    }
}
