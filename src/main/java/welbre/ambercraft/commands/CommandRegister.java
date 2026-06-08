package welbre.ambercraft.commands;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class CommandRegister {
    public static void register() {
        NeoForge.EVENT_BUS.addListener(CommandRegister::CommandRegister);
    }

    public static void CommandRegister(RegisterCommandsEvent event)
    {

    }
}
