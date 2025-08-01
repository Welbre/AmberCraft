package welbre.ambercraft.commands;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class Event {
    public static void register() {
        NeoForge.EVENT_BUS.addListener(Event::CommandRegister);
    }

    public static void CommandRegister(RegisterCommandsEvent event)
    {

    }
}
