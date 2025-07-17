package welbre.ambercraft;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class ServerEvents {
    public static void register() {
        NeoForge.EVENT_BUS.addListener(ServerEvents::CommandRegister);
    }

    public static void CommandRegister(RegisterCommandsEvent event)
    {

    }
}
