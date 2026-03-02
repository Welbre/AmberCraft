package welbre.ambercraft.subblock.client;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import welbre.ambercraft.subblock.SubBlockBE;

import static welbre.ambercraft.AmberCraft.MOD_ID;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = MOD_ID)
public final class SubBlockServerEventListener
{
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
    {
        if (event.getAction() == PlayerInteractEvent.LeftClickBlock.Action.START && event.getLevel().getBlockEntity(event.getPos()) instanceof SubBlockBE sub && event.getEntity() instanceof Player player)
        {
            var state = sub.getStateByRayCast(player);
            if (state != null)
            {
                sub.dropTinyState(state);
                event.setCanceled(true);
                System.out.println(state.serializeNBT(null).toString() + " \n " + event.getAction().toString() + "\n " + event.getLevel().isClientSide());
            }
        }
    }
}
