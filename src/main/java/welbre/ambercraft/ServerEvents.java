package welbre.ambercraft;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import welbre.ambercraft.sim.network.Network;

public class ServerEvents {
    public static void register() {
        NeoForge.EVENT_BUS.addListener(ServerEvents::LEVEL_TICK_EVENT);
        NeoForge.EVENT_BUS.addListener(ServerEvents::CommandRegister);
        NeoForge.EVENT_BUS.addListener(ServerEvents::CLOSE_THE_WORLD);
        NeoForge.EVENT_BUS.addListener(ServerEvents::OPEN_THE_WORLD);
    }

    public static void LEVEL_TICK_EVENT(LevelTickEvent.Pre event)
    {
        Level level = event.getLevel();
        if (!level.isClientSide)
            Network.TICK_ALL();
    }

    public static void CommandRegister(RegisterCommandsEvent event)
    {

    }






    public static void OPEN_THE_WORLD(LevelEvent.Load event)
    {
        if (event.getLevel() instanceof ServerLevel level)
        {
            level.getDataStorage().get(new SavedData.Factory<>(NET_DATA::new, NET_DATA::LOAD),"network_data");
        }
    }

    public static void CLOSE_THE_WORLD(LevelEvent.Unload event)
    {
        if (event.getLevel() instanceof ServerLevel level)
        {
            level.getDataStorage().set("network_data", new NET_DATA());
        }
    }



    
    private static final class NET_DATA extends SavedData
    {
        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            Network.SAVE_DATA(tag,true);
            return tag;
        }

        public static SavedData LOAD(CompoundTag tag, HolderLookup.Provider provider) {
            Network.LOAD_DATA(tag);
            return new NET_DATA();
        }
    }
}
