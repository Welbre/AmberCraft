package welbre.ambercraft.item;

import io.netty.buffer.Unpooled;
import kuse.welbre.sim.electrical.Circuit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.client.AmberCraftScreenHelper;
import welbre.ambercraft.item.components.MultimeterComponent;
import welbre.ambercraft.module.electrical.ElectricalCableModule;
import welbre.ambercraft.module.electrical.ElectricalElementModule;
import welbre.ambercraft.module.electrical.ElectricalModulesMaster;
import welbre.ambercraft.module.electrical.ElectricalTerminalModule;
import welbre.ambercraft.module.network.NetworkModule;
import welbre.ambercraft.network.OscilloscopeDataPayload;

import java.util.HashSet;
import java.util.UUID;

public class OscilloscopeItem extends MultimeterItem
{
    //used to store the player uuid in the oscilloscope section.
    public static final HashSet<UUID> WATCHERS = new HashSet<>();

    public OscilloscopeItem(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull InteractionResult handleVoltage(boolean contains, MultimeterComponent component, ServerPlayer player, NetworkModule module)
    {
        final Circuit.Pin preview = contains ? pinMap.remove(component.id()) : null;//if contains remove, else just assign with anything
        switch (module)
        {
            case ElectricalCableModule ecm ->
            {
                if (contains)
                    initWatcher(player, (ElectricalModulesMaster) ecm.getRoot().getMaster(), ecm.getTerminal()[0], preview);
                else
                    mapPin(component.id(), ecm.getTerminal()[0], player);
                return InteractionResult.SUCCESS;
            }
            case ElectricalTerminalModule etm ->
            {
                if (contains)
                    initWatcher(player, (ElectricalModulesMaster) etm.getRoot().getMaster(), etm.getTerminal()[0], preview);
                else
                    mapPin(component.id(), etm.getTerminal()[0], player);
                return InteractionResult.SUCCESS;
            }
            case ElectricalElementModule eem ->
            {
                initWatcher(player, (ElectricalModulesMaster) eem.getRoot().getMaster(), eem.getTerminalA().getTerminal()[0], eem.getTerminalB().getTerminal()[0]);
                return InteractionResult.SUCCESS;
            }
            case null, default ->
            {
                return InteractionResult.FAIL;
            }
        }
    }

    protected void initWatcher(ServerPlayer player, ElectricalModulesMaster master, Circuit.Pin a, Circuit.Pin b)
    {
        if (a == b)
        {
            player.sendSystemMessage(Component.translatable(MSG_VOLTAGE_SAME_SPLOT).withColor(DyeColor.RED.getTextColor()));
            return;
        }

        //start the scheduler that will update the oscilloscope data.
        master.scheduler.scheduleEachTick(0, 0, 99999999, (s) -> {}, task -> {
            if (SHOULD_END_WATCHER(player))
            {
                task.markToRemove();
                WATCHERS.remove(player.getUUID());
                return;
            }

            double va = 0, vb = 0;
            if (a != null && a.P_voltage != null)
                va = a.P_voltage[0];
            if (b != null && b.P_voltage != null)
                vb = b.P_voltage[0];

            PacketDistributor.sendToPlayer(player, new OscilloscopeDataPayload(vb-va));
        });
        openOscilloscopeScreen(player);
    }

    /// Returns if the player isn't in a valid state to keep using the oscilloscope.
    public static boolean SHOULD_END_WATCHER(ServerPlayer player)
    {
        //check if the player still in the server
        if (player.hasDisconnected())
            return true;
        //check if the player is holding the oscilloscope.
        if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() != AmberCraft.Items.OSCILLOSCOPE.get())
            return true;
        //check if the player still the watchers set, if not, the server receives an OscilloscopeClosedPayload because the player closes the screen.
        if (!WATCHERS.contains(player.getUUID()))
            return true;
        return false;
    }

    public void openOscilloscopeScreen(ServerPlayer player)
    {
        WATCHERS.add(player.getUUID());
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        AmberCraftScreenHelper.openInClient(AmberCraftScreenHelper.TYPES.OSCILLOSCOPE, buf, player);
    }
}
