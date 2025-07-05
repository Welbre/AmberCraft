package welbre.ambercraft;

import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import welbre.ambercraft.sim.network.Network;

import java.io.*;

public class ServerEvents {
    public static void LEVEL_TICK_EVENT(LevelTickEvent.Pre event)
    {
        Level level = event.getLevel();
        if (!level.isClientSide)
            Network.TICK_ALL();
    }

    public static void CommandRegister(RegisterCommandsEvent event)
    {

    }

    public static void CLOSE_THE_WORLD(LevelEvent.Unload event)
    {
        if (true)
            return;
        try
                //todo save extra data when the world close.
        {
            File networkSave = new File(
                    "saves/"+
                    event.getLevel().getServer().getWorldData().getLevelName()
                    +"/amber/network.file");
            networkSave.createNewFile();
            var writer = new BufferedOutputStream(new FileOutputStream(networkSave));

            Network.FLUSH_DATA(writer,true);

            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
