package welbre.ambercraft.item;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.elements.Resistor;
import kuse.welbre.tools.Tools;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.item.components.MultimeterComponent;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.electrical.ElectricalCableModule;
import welbre.ambercraft.module.electrical.ElectricalElementModule;
import welbre.ambercraft.module.electrical.ElectricalModule;
import welbre.ambercraft.module.electrical.ElectricalTerminalModule;
import welbre.ambercraft.module.network.NetworkModule;

import java.util.*;
import java.util.function.BiConsumer;

import static welbre.ambercraft.AmberCraft.Components.MULTIMETER_CACHE_DATA_COMPONENT;

public class MultimeterItem extends Item {
    protected static final String MSG_TOO_FAR = "item.ambercraft.multimeter.too_far";
    protected static final String MSG_MODE_CHANGED = "item.ambercraft.multimeter.mode_changed";
    protected static final String MSG_VOLTAGE = "item.ambercraft.multimeter.voltage";
    protected static final String MSG_CURRENT= "item.ambercraft.multimeter.current";
    protected static final String MSG_POWER = "item.ambercraft.multimeter.power";
    protected static final String MSG_RESISTENCE = "item.ambercraft.multimeter.resistance";
    protected static final String MSG_FIRST_CLICK = "item.ambercraft.multimeter.first_click";
    protected static final String MSG_VOLTAGE_SAME_SPLOT = "item.ambercraft.multimeter.voltage_same_spot";
    protected static final String MSG_CURRENT_SAME_TERMINAL = "item.ambercraft.multimeter.current_same_terminal";
    protected static final String MSG_CURRENT_DIF_ELEMENT = "item.ambercraft.multimeter.current_dif_element";
    protected static final String MSG_CURRENT_DIF_CIRCUIT = "item.ambercraft.multimeter.current_dif_circuit";
    protected static final String MSG_CORRUPTION = "item.ambercraft.multimeter.corruption";
    protected static final String MSG_INTERNAL_ERROR = "item.ambercraft.multimeter.internal_error";

    protected static final BiConsumer<ServerPlayer, Double> SEND_VOLTAGE_IN_CHAT = (player, voltage) -> player.sendSystemMessage(Component.translatable(MSG_VOLTAGE, Tools.proprietyToSi(voltage, "V")) );
    protected static final BiConsumer<ServerPlayer, Double> SEND_CURRENT_IN_CHAT = (player, current) -> player.sendSystemMessage(Component.translatable(MSG_CURRENT, Tools.proprietyToSi(current, "A")));
    protected static final BiConsumer<ServerPlayer, Double> SEND_POWER_IN_CHAT = (player, power) -> player.sendSystemMessage(Component.translatable(MSG_POWER, Tools.proprietyToSi(power, "W")));
    protected static final BiConsumer<ServerPlayer, Double> SEND_RESISTENCE_IN_CHAT = (player, power) -> player.sendSystemMessage(Component.translatable(MSG_RESISTENCE, Tools.proprietyToSi(power, "Ω")));
    protected static BiConsumer<ServerPlayer, Double> SEND_VOLTAGE_ACTION = SEND_VOLTAGE_IN_CHAT;
    protected static BiConsumer<ServerPlayer, Double> SEND_CURRENT_ACTION = SEND_CURRENT_IN_CHAT;
    protected static Double VOLTAGE_VALUE = null;
    protected static Double CURRENT_VALUE = null;
    protected static final BiConsumer<ServerPlayer, Double> VOLTAGE_GETTER = ((player, v) -> VOLTAGE_VALUE = v);
    protected static final BiConsumer<ServerPlayer, Double> CURRENT_GETTER = ((player, v) -> CURRENT_VALUE = v);
    protected static boolean ignoreFirstClickMessage = false;

    public static final HashMap<UUID, Circuit.Pin> pinMap = new HashMap<>();
    public static final HashMap<UUID, NetworkModule> moduleMap = new HashMap<>();

    public MultimeterItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        //click in the ar, change the multimeter mode
        if (player instanceof ServerPlayer sPlayer)
        {
            ItemStack stack = player.getItemInHand(hand);
            MultimeterComponent component = PUT_MODULE_IF_ABSTENDE(stack);

            component = new MultimeterComponent(component.id(), component.mode().next());

            stack.set(MULTIMETER_CACHE_DATA_COMPONENT.get(), component);
            sPlayer.sendSystemMessage(Component.translatable(MSG_MODE_CHANGED, component.mode().name()));

            return InteractionResult.SUCCESS;
        }

        return super.use(level, player, hand);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        final boolean client =  context.getLevel().isClientSide;
        if (context.getPlayer() == null)
            return InteractionResult.PASS;

        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof ModulesHolder holder)
        {
            List<NetworkModule> modules = filterModules(holder.getModule(context.getClickedFace()));
            if (!modules.isEmpty())
            {
                if (!client)
                    for (NetworkModule module : modules)
                    {
                        handle(context.getItemInHand(), (ServerPlayer) context.getPlayer(), module);
                    }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;//avoids the mode change in the case that the player has clicked in a face that doesn't have a module
        }
        return super.useOn(context);
    }

    /// Server side only!
    public @NotNull InteractionResult handle(ItemStack stack, ServerPlayer player, NetworkModule module)
    {
        MultimeterComponent component = PUT_MODULE_IF_ABSTENDE(stack);

        return switch (component.mode())
        {
            case Voltage -> handleVoltage(pinMap.containsKey(component.id()), component, player, module);
            case Current -> handleCurrent(moduleMap.containsKey(component.id()), component, player, module);
            case Power -> handlePower(moduleMap.containsKey(component.id()), component, player, module);
            case Resistence -> handleResistence(moduleMap.containsKey(component.id()), component, player, module);
            case null -> InteractionResult.FAIL;
        };
    }

    protected @NotNull InteractionResult handleVoltage(final boolean contains, MultimeterComponent component, ServerPlayer player, NetworkModule module)
    {
        final Circuit.Pin preview = contains ? pinMap.remove(component.id()) : null;//if contains remove, else just assign with anything
        switch (module)
        {
            case ElectricalCableModule ecm ->
            {
                if (contains)
                    sendVdiff(player, ecm.getTerminal()[0], preview);
                else
                    mapPin(component.id(), ecm.getTerminal()[0], player);
                return InteractionResult.SUCCESS;
            }
            case ElectricalTerminalModule etm ->
            {
                if (contains)
                    sendVdiff(player, etm.getTerminal()[0], preview);
                else
                    mapPin(component.id(), etm.getTerminal()[0], player);
                return InteractionResult.SUCCESS;
            }
            case ElectricalElementModule eem ->
            {
                sendVdiff(player, eem.getTerminalA().getTerminal()[0], eem.getTerminalB().getTerminal()[0]);
                return InteractionResult.SUCCESS;
            }
            case null, default ->
            {
                return InteractionResult.FAIL;
            }
        }
    }

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
                sendCurrent(eem.getElement(), eem.getTerminalA().getTerminal()[0], player);
                return InteractionResult.SUCCESS;
            }
            case null, default ->
            {
                return InteractionResult.FAIL;
            }
        }
    }

    protected @NotNull InteractionResult handlePower(final boolean contains, MultimeterComponent component, ServerPlayer player, NetworkModule module)
    {
        InteractionResult result = GET_VOLTAGE_AND_CURRENT(contains, component, player, module);
        if (!result.consumesAction()) return InteractionResult.FAIL;
        if (VOLTAGE_VALUE == null || CURRENT_VALUE == null) return InteractionResult.FAIL;

        SEND_POWER_IN_CHAT.accept(player, VOLTAGE_VALUE * CURRENT_VALUE);
        VOLTAGE_VALUE = null;
        CURRENT_VALUE = null;
        return InteractionResult.SUCCESS;
    }

    protected @NotNull InteractionResult handleResistence(final boolean contains, MultimeterComponent component, ServerPlayer player, NetworkModule module)
    {
        InteractionResult result = GET_VOLTAGE_AND_CURRENT(contains, component, player, module);
        if (!result.consumesAction()) return InteractionResult.FAIL;
        if (VOLTAGE_VALUE == null || CURRENT_VALUE == null) return InteractionResult.FAIL;

        SEND_RESISTENCE_IN_CHAT.accept(player, VOLTAGE_VALUE / CURRENT_VALUE);
        VOLTAGE_VALUE = null;
        CURRENT_VALUE = null;
        return InteractionResult.SUCCESS;
    }

    protected void mapPin(UUID id, Circuit.Pin pin, ServerPlayer player)
    {
        pinMap.put(id, pin);
        if (!ignoreFirstClickMessage)
            player.sendSystemMessage(Component.translatable(MSG_FIRST_CLICK).withColor(DyeColor.LIME.getTextColor()));
    }

    protected void mapModule(UUID id, NetworkModule module, ServerPlayer player)
    {
        moduleMap.put(id, module);
        if (!ignoreFirstClickMessage)
            player.sendSystemMessage(Component.translatable(MSG_FIRST_CLICK).withColor(DyeColor.LIME.getTextColor()));
    }

    protected void sendVdiff(ServerPlayer player, Circuit.Pin a, Circuit.Pin b)
    {
        if (a == b)
        {
            player.sendSystemMessage(Component.translatable(MSG_VOLTAGE_SAME_SPLOT).withColor(DyeColor.RED.getTextColor()));
            return;
        }
        double va = 0, vb = 0;
        if (a != null && a.P_voltage != null)
            va = a.P_voltage[0];
        if (b != null && b.P_voltage != null)
            vb = b.P_voltage[0];

        SEND_VOLTAGE_ACTION.accept(player, vb - va);
    }

    protected @NotNull InteractionResult GET_VOLTAGE_AND_CURRENT(final boolean contains, MultimeterComponent component, ServerPlayer player, NetworkModule module)
    {
        ignoreFirstClickMessage = true;
        //collects the voltage to the VOLTAGE_VALUE
        SEND_VOLTAGE_ACTION = VOLTAGE_GETTER;
        InteractionResult result = handleVoltage(contains, component, player, module);
        SEND_VOLTAGE_ACTION = SEND_VOLTAGE_IN_CHAT;

        if (!result.consumesAction())
        {
            ignoreFirstClickMessage = false;
            return InteractionResult.FAIL;
        }

        //collects the current to the CURRENT_VALUE
        SEND_CURRENT_ACTION = CURRENT_GETTER;
        InteractionResult result0 = handleCurrent(contains, component, player, module);
        SEND_CURRENT_ACTION = SEND_CURRENT_IN_CHAT;

        ignoreFirstClickMessage = false;
        if (result0.consumesAction())
        {
            if (!contains)
                player.sendSystemMessage(Component.translatable(MSG_FIRST_CLICK).withColor(DyeColor.LIME.getTextColor()));
            return InteractionResult.SUCCESS;
        }
        else
            return InteractionResult.FAIL;
    }

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
                        sendCurrent(element.getElement(), etm.getTerminal()[0], player);
                    }
                    else
                        player.sendSystemMessage(Component.translatable(MSG_CORRUPTION).withColor(DyeColor.RED.getTextColor()));
                }
            }
        }
        else if (preview instanceof ElectricalCableModule ecm)
            sendCableTerminalCurrent(player, ecm, etm);
    }

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
                sendCurrent(Math.abs(resistor.getCurrent()), player);
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

    protected Resistor getResistorInCableTerminal(ElectricalCableModule ecm, ElectricalTerminalModule etm)
    {
        final List<Resistor> resistors = FIND_RESISTOR(ecm);
        final Circuit.Pin pin = etm.getTerminal()[0];

        final List<Resistor> filtered = resistors.stream().filter(r -> r.getPinA() == pin || r.getPinB() == pin).toList();

        if (filtered.isEmpty())
            return null;

        return filtered.getFirst();
    }

    /// from a cable to a terminal, the cable is the preview "first clicked"!
    protected void sendCableTerminalCurrent(ServerPlayer player, ElectricalCableModule ecm, ElectricalTerminalModule etm)
    {
        final Resistor resistor = getResistorInCableTerminal(ecm, etm);
        if (resistor == null)
        {
            player.sendSystemMessage(Component.translatable(MSG_TOO_FAR));
            return;
        }
        sendCurrent(resistor, etm.getTerminal()[0], player);
    }

    protected void sendTerminalCableCurrent(ServerPlayer player, ElectricalTerminalModule etm, ElectricalCableModule ecm)
    {
        final Resistor resistor = getResistorInCableTerminal(ecm, etm);
        if (resistor == null)
        {
            player.sendSystemMessage(Component.translatable(MSG_TOO_FAR));
            return;
        }
        sendCurrent(resistor, ecm.getTerminal()[0], player);
    }

    protected void sendCableCableCurrent(ServerPlayer player, ElectricalCableModule ecm, ElectricalCableModule preview)
    {
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
            player.sendSystemMessage(Component.translatable(MSG_INTERNAL_ERROR).withColor(0xffdd0000));
            return;
        }

        sendCurrent(common, ecm.getTerminal()[0], player);
    }

    protected void sendCurrent(Element element, Circuit.Pin positivePin, ServerPlayer player)
    {
        final double current;
        if (element.getPinA() == positivePin)
            current = -element.getCurrent();
        else
            current = element.getCurrent();

        sendCurrent(current, player);
    }

    protected void sendCurrent(double current, ServerPlayer player)
    {
        SEND_CURRENT_ACTION.accept(player, current);
    }

    /// Find all resistors that are connected to the ElectricalCableModule
    protected static List<Resistor> FIND_RESISTOR(ElectricalCableModule ecm)
    {
        //we need to iterate in all neighbors because the resistor only is one of the cables in the connection
        HashSet<Resistor> resistors = new HashSet<>(Arrays.asList(ecm.getResistors()));
        for (NetworkModule n : ecm.getNeighbors())
            if (n instanceof ElectricalCableModule neighbor)
                resistors.addAll(Arrays.asList(neighbor.getResistors()));

        //the pin of this cable
        final Circuit.Pin pin = ecm.getTerminal()[0];
        //all resistors that are connected to the pin.
        return resistors.stream().filter(r -> r.getPinA() == pin || r.getPinB() == pin).toList();
    }

    /// Return the resistor that has more absolute current flowing in it.
    protected static Resistor FIND_BIGEST_RESISTOR(ElectricalCableModule ecm)
    {
        //all resistors that are connected to the pin sorted by the current value.
        List<Resistor> connectedTo = FIND_RESISTOR(ecm).stream().sorted(Comparator.comparing(Resistor::getCurrent)).toList();

        //check of cable that is unconnected.
        if (connectedTo.size() >= 2)
        {
            Resistor high = connectedTo.getFirst();
            Resistor low = connectedTo.getLast();

            //send the biggest current by module.
            if (Math.abs(high.getCurrent()) > Math.abs(low.getCurrent()))
                return high;
            else
                return low;
        }
        return null;
    }

    protected static MultimeterComponent PUT_MODULE_IF_ABSTENDE(ItemStack stack)
    {
        MultimeterComponent component = stack.getComponents().get(MULTIMETER_CACHE_DATA_COMPONENT.get());

        if (component == null)
        {
            component = new MultimeterComponent();
            stack.set(MULTIMETER_CACHE_DATA_COMPONENT.get(), component);
        }

        return component;
    }

    protected List<NetworkModule> filterModules(Module[] modules)
    {
        return Arrays.stream(modules)
                .filter(a -> a instanceof NetworkModule)
                .map(a -> (NetworkModule) a)
                .filter(a -> (a instanceof ElectricalTerminalModule) || (a instanceof ElectricalModule)).toList();
    }
}
