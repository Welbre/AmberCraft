package welbre.ambercraft.item;

import io.netty.buffer.Unpooled;
import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.elements.Resistor;
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

import java.util.*;
import java.util.function.Supplier;

public class OscilloscopeItem extends MultimeterItem
{
    //used to store the player uuid in the oscilloscope section.
    public static final HashSet<UUID> WATCHERS = new HashSet<>();

    public OscilloscopeItem(Properties properties) {
        super(properties);
    }

    //----------------------------------------------------Voltage

    @Override
    protected @NotNull InteractionResult handleVoltage(boolean contains, MultimeterComponent component, ServerPlayer player, NetworkModule module)
    {
        final Circuit.Pin preview = contains ? pinMap.remove(component.id()) : null;//if contains remove, else just assign with anything
        switch (module)
        {
            case ElectricalCableModule ecm ->
            {
                if (contains)
                {
                    if (checkForSamePin(ecm.getTerminal()[0], preview, player))
                        initWatcher(player, (ElectricalModulesMaster) ecm.getRoot().getMaster(), GET_VOLTAGE_SUPPLIER(ecm.getTerminal()[0], preview));
                }
                else
                    mapPin(component.id(), ecm.getTerminal()[0], player);
                return InteractionResult.SUCCESS;
            }
            case ElectricalTerminalModule etm ->
            {
                if (contains)
                    initWatcher(player, (ElectricalModulesMaster) etm.getElectrical().getRoot().getMaster(), GET_VOLTAGE_SUPPLIER(etm.getTerminal()[0], preview));
                else
                    mapPin(component.id(), etm.getTerminal()[0], player);
                return InteractionResult.SUCCESS;
            }
            case ElectricalElementModule eem ->
            {
                initWatcher(player, (ElectricalModulesMaster) eem.getRoot().getMaster(), GET_VOLTAGE_SUPPLIER(eem.getTerminalA().getTerminal()[0], eem.getTerminalB().getTerminal()[0]));
                return InteractionResult.SUCCESS;
            }
            case null, default ->
            {
                return InteractionResult.FAIL;
            }
        }
    }

    public boolean checkForSamePin(Circuit.Pin a, Circuit.Pin b, ServerPlayer player)
    {
        if (a == b)
        {
            player.sendSystemMessage(Component.translatable(MSG_VOLTAGE_SAME_SPLOT).withColor(DyeColor.RED.getTextColor()));
            return false;
        }
        return true;
    }

    public static Supplier<Double> GET_VOLTAGE_SUPPLIER(Circuit.Pin a, Circuit.Pin b)
    {
        return () -> {
            double va = 0, vb = 0;
            if (a != null && a.P_voltage != null)
                va = a.P_voltage[0];
            if (b != null && b.P_voltage != null)
                vb = b.P_voltage[0];

            return vb-va;
        };
    }

    //----------------------------------------------------Current

    @Override
    protected void sendCableCurrent(ServerPlayer player, ElectricalCableModule ecm, NetworkModule preview)
    {
        //terminal modules are volatility, the circuit isn't connected to it, it is a warper.
        if (!(preview instanceof ElectricalTerminalModule) && ecm.getRoot() != preview.getRoot())
        {
            player.sendSystemMessage(Component.translatable(MSG_CURRENT_DIF_CIRCUIT).withColor(DyeColor.RED.getTextColor()));
            return;
        }

        //measuring the current in one cable
        if (ecm == preview)
        {
            Resistor resistor = FIND_BIGEST_RESISTOR(ecm);
            if (resistor != null)
                initWatcher(player, (ElectricalModulesMaster) ecm.getRoot().getMaster(), GET_CURRENT_SUPPLIER(resistor, null));
        }
        else //is in the same network but are different modules.
        {
            //check if ecm and preview is neighbors.
            //terminal modules are volatility, the circuit isn't connected to it, it is a warper.
            if (!(preview instanceof ElectricalTerminalModule) && Arrays.stream(ecm.getNeighbors()).noneMatch(a -> a == preview))
            {
                player.sendSystemMessage(Component.translatable(MSG_TOO_FAR));
                return;
            }

            if (preview instanceof ElectricalCableModule module)
                sendCableCableCurrent(player, ecm, module);
            else if (preview instanceof ElectricalTerminalModule module)
                sendTerminalCableCurrent(player, module, ecm);
        }
    }

    @Override
    protected void sendTerminalCurrent(ServerPlayer player, ElectricalTerminalModule etm, NetworkModule preview)
    {
        if (preview instanceof ElectricalTerminalModule module)
        {
            if (etm == module)
                player.sendSystemMessage(Component.translatable(MSG_CURRENT_SAME_TERMINAL).withColor(DyeColor.RED.getTextColor()));
            else
            {
                if (etm.getElectrical().ID != module.getElectrical().ID)
                    player.sendSystemMessage(Component.translatable(MSG_CURRENT_DIF_ELEMENT).withColor(DyeColor.RED.getTextColor()));
                else
                {
                    if (etm.getElectrical() instanceof ElectricalElementModule element)
                    {
                        initWatcher(player, (ElectricalModulesMaster) element.getRoot().getMaster(), GET_CURRENT_SUPPLIER(element.getElement(), etm.getTerminal()[0]));
                    }
                    else
                        player.sendSystemMessage(Component.literal("Corruption in the circuit formation :(, send this message to the devs!").withColor(DyeColor.RED.getTextColor()));
                }
            }
        }
        else if (preview instanceof ElectricalCableModule ecm)
            sendCableTerminalCurrent(player, ecm, etm);
    }

    @Override
    protected @NotNull InteractionResult handleCurrent(final boolean contains, MultimeterComponent component, ServerPlayer player, NetworkModule module)
    {
        final NetworkModule preview = contains ? moduleMap.remove(component.id()) : null;//if contains remove, else just assign with anything
        switch (module)
        {
            case ElectricalCableModule ecm ->
            {
                if (contains)
                    sendCableCurrent(player, ecm, preview);
                else
                    mapModule(component.id(), ecm, player);
                return InteractionResult.SUCCESS;
            }
            case ElectricalTerminalModule etm ->
            {
                if (contains)
                    sendTerminalCurrent(player, etm, preview);
                else
                    mapModule(component.id(), etm, player);
                return InteractionResult.SUCCESS;
            }
            case ElectricalElementModule eem ->
            {
                initWatcher(player, (ElectricalModulesMaster) eem.getRoot().getMaster(), GET_CURRENT_SUPPLIER(eem.getElement(), eem.getTerminalA().getTerminal()[0]));
                return InteractionResult.SUCCESS;
            }
            case null, default ->
            {
                return InteractionResult.FAIL;
            }
        }
    }

    @Override
    protected void sendCableTerminalCurrent(ServerPlayer player, ElectricalCableModule ecm, ElectricalTerminalModule etm) {
        final Resistor resistor = getResistorInCableTerminal(ecm, etm);
        if (resistor == null)
        {
            player.sendSystemMessage(Component.translatable(MSG_TOO_FAR));
            return;
        }

        initWatcher(player, (ElectricalModulesMaster) ecm.getRoot().getMaster(), GET_CURRENT_SUPPLIER(resistor, etm.getTerminal()[0]));
    }

    @Override
    protected void sendTerminalCableCurrent(ServerPlayer player, ElectricalTerminalModule etm, ElectricalCableModule ecm) {
        final Resistor resistor = getResistorInCableTerminal(ecm, etm);
        if (resistor == null)
        {
            player.sendSystemMessage(Component.translatable(MSG_TOO_FAR));
            return;
        }
        initWatcher(player, (ElectricalModulesMaster) ecm.getRoot().getMaster(), GET_CURRENT_SUPPLIER(resistor, ecm.getTerminal()[0]));
    }

    @Override
    protected void sendCableCableCurrent(ServerPlayer player, ElectricalCableModule ecm, ElectricalCableModule preview) {
        List<Resistor> resistors = new ArrayList<>();
        resistors.addAll(FIND_RESISTOR(preview));
        resistors.addAll(FIND_RESISTOR(ecm));
        Resistor common = null;

        head:
        for (int i = 0; i < resistors.size(); i++)
        {
            Resistor ri = resistors.get(i);
            Circuit.Pin a = ri.getPinA(), b = ri.getPinB();
            for (int j = i+1; j < resistors.size(); j++)
            {
                Resistor rj = resistors.get(j);
                Circuit.Pin a0 = rj.getPinA(), b0 = rj.getPinB();
                if (a == a0 || a == b0)
                    if (b == a0 || b == b0)
                    {
                        common = ri;
                        break head;
                    }
            }
        }

        //check to avoid crashs
        if (common == null)
        {
            player.sendSystemMessage(Component.literal("Internal Error").withColor(0xffdd0000));
            return;
        }

        initWatcher(player, (ElectricalModulesMaster) ecm.getRoot().getMaster(), GET_CURRENT_SUPPLIER(common, ecm.getTerminal()[0]));
    }

    /// @param positivePin the positive direction of the current, if is null, returns the absolute value.
    public static Supplier<Double> GET_CURRENT_SUPPLIER(Element element, Circuit.Pin positivePin)
    {
        if (positivePin == null)
            return () -> Math.abs(element.getCurrent());
        else
            return () -> {
                if (element.getPinA() == positivePin)
                    return -element.getCurrent();
                else
                    return element.getCurrent();
            };
    }

    protected void initWatcher(ServerPlayer player, ElectricalModulesMaster master, Supplier<Double> dataSupplier)
    {
        System.out.println("created!");
        //start the scheduler that will update the oscilloscope data.
        master.scheduler.scheduleEachTick(0, 0, 99999999, (s) -> {}, task -> {
            if (SHOULD_END_WATCHER(player))
            {
                task.markToRemove();
                WATCHERS.remove(player.getUUID());
                return;
            }

            PacketDistributor.sendToPlayer(player, new OscilloscopeDataPayload(dataSupplier.get()));
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
