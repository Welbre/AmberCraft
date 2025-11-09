package welbre.ambercraft.module.electrical;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.tools.Tools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.electrical.ElectricalBE;
import welbre.ambercraft.module.DebugToolInfo;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.network.Master;
import welbre.ambercraft.module.network.NetworkModule;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <h5>This module is used to store/handle a {@link Element electricalElement}.</h5>
 * Notice that {@link ElectricalElementModule#terminalA} and {@link ElectricalElementModule#terminalB} are modules too,
 * any it is always connected to this module! <a color="red">Don't disconnect it!</a>.<br>
 * The {@link  ElectricalElementModule#element} is the most important field, it stores the electrical element that will be used in the {@link kuse.welbre.sim.electrical.Circuit}.<br><br>
 * Basically to use this module, you return it in {@link ModulesHolder#getModules()} and <b>NEVER</b> return this module in another place,
 * and returns the {@link ElectricalElementModule#terminalA} and {@link ElectricalElementModule#terminalB} in {@link ModulesHolder#getModule(Direction)}.
 * But why? This module doesn't connect directly to others, instead, they use the {@link ElectricalTerminalModule} as a wrapper, each <code>ETM</code> is designed to connect/disconnect the {@link #element}.
 * Therefore, if you return this module in the holder, it will connect the module but won't connect the element, setting this module in an illegal state.
 *
 * @see ElectricalTerminalModule
 */
public class ElectricalElementModule extends ElectricalModule implements DebugToolInfo {
    private Element element;
    private ElectricalTerminalModule terminalA;
    private ElectricalTerminalModule terminalB;

    public ElectricalElementModule()
    {
        terminalA = new ElectricalTerminalModule(this);
        terminalB = new ElectricalTerminalModule(this);
        connect(terminalA);
        connect(terminalB);
    }

    public ElectricalElementModule(Element element)
    {
        setElement(element);
    }

    public void setElement(Element e)
    {
        element = e;
    }

    public Element getElement() {
        return element;
    }

    @Override
    public boolean connect(NetworkModule target) {

        return super.connect(target);
    }

    @Override
    public void disconnectAll() {
        if (element != null)
            setElement(element);
        super.disconnectAll();
    }

    public ElectricalTerminalModule getTerminalA() {
        return terminalA;
    }

    public ElectricalTerminalModule getTerminalB() {
        return terminalB;
    }

    //-------------------------------------------------------------------------------------------------------------------------------//
    //-------------------------------------------------------------Data--------------------------------------------------------------//
    //-------------------------------------------------------------------------------------------------------------------------------//

    @Override
    public void writeData(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeData(tag, registries);
        if (element != null)
        {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            DataOutputStream data = new DataOutputStream(stream);
            try
            {

                element.serialize(data);
                data.close();
                stream.close();
                tag.putByteArray("element", stream.toByteArray());
                tag.putString("elementClass", element.getClass().getSimpleName());
            } catch (IOException e)
            {
                AmberCraft.LOGGER.error("ElectricalModule: Failed to serialize element!", e);
            }
        }
    }

    @Override
    public void readData(CompoundTag tag, HolderLookup.Provider registries) {
        super.readData(tag, registries);
        if (tag.contains("elementClass"))
        {
            try
            {
                Class<?> clazz = Class.forName("kuse.welbre.sim.electrical.elements." + tag.getString("elementClass"));
                Object object = clazz.getConstructor().newInstance();
                if (object instanceof Element el)
                {
                    DataInputStream data = new DataInputStream(new ByteArrayInputStream(tag.getByteArray("element")));
                    el.unSerialize(data);
                    data.close();

                    setElement(el);
                } else {
                    throw new RuntimeException("ElectricalModule: Element class is not an instance of Element! Expected %s founded %s".formatted(Element.class.getName(), object.getClass().getName()));
                }
            } catch (Exception e)
            {
                AmberCraft.LOGGER.error("ElectricalModule: Failed to deserialize element!", e);
            }
        }
    }

    @Override
    public void alloc(){}

    @Override
    public Element[] compile()
    {
        if (element != null)
        {
            element.connect(terminalA.terminal[0], terminalB.terminal[0]);
            return new Element[]{element};
        }
        return new Element[0];
    }

    @Override
    public void free()
    {
        if (terminalA != null)
            terminalA.disconnectAll();
        if (terminalB != null)
            terminalB.disconnectAll();
        element = null;
        terminalA = null;
        terminalB = null;
        disconnectAll();
    }

    @Override
    public void onLoad(ModulesHolder entity)
    {
        if (isFresh)
            return;

        var level = entity.getLevel();
        if (level == null)
            throw new IllegalStateException("Trying to refresh module while the game isn't loaded!");
        if (level.isClientSide())
            return;

        //the modulesHolder don't run the terminal logic, don't remove this from this method.
        terminalA.onLoad(entity);
        terminalB.onLoad(entity);
        // connect with surroundings
        if (entity instanceof ElectricalBE be)
            for (Direction dir : Direction.values())
                if (be.getModule(dir).length > 0 && level.getBlockEntity(be.getBlockPos().relative(dir)) instanceof ModulesHolder holder)
                    for (Module module : be.getModule(dir))
                        if (module instanceof NetworkModule networkModule)
                            for (Module other : holder.getModule(dir.getOpposite()))
                                if (other instanceof NetworkModule otherNetworkModule)
                                    otherNetworkModule.connect(networkModule);

        isFresh = true;
    }

    @Override
    public Master createMaster() {
        return new ElectricalModulesMaster(this);
    }

    @Override
    public void tick(ModulesHolder entity) {
        if (!this.isMaster())
            return;
        master.tick(entity);
    }

    @Override
    public List<Component> getInfo() {
        if (element == null)
            return List.of(Component.literal("Element is null!"));
        List<Component> list = new ArrayList<>();

        final Circuit.Pin pinA = element.getPinA();
        final Circuit.Pin pinB = element.getPinB();
        list.add(GET_PIN_INFO("pinA: %s, Voltage: %s", pinA));
        list.add(GET_PIN_INFO("pinB: %s, Voltage: %s", pinB));

        list.add(GET_ELEMENT_INFO(element));

        return list;
    }

    public static Component GET_PIN_INFO(String pattern, Circuit.Pin pin)
    {
        return Component.literal(pattern.formatted(
                pin == null ? "gnd" : pin.address,
                pin == null ? "gnd" : (pin.P_voltage != null ? Tools.proprietyToSi(pin.P_voltage[0], "V") : "NaN")
        ));
    }

    public static Component GET_ELEMENT_INFO(Element element)
    {
        if (element == null)
            return Component.literal("Element is null");
        else
            return Component.empty()
                    .append(element.getClass().getSimpleName()).withColor(DyeColor.PURPLE.getTextColor())//element name
                    .append(Component.literal("(%s)".formatted(Tools.proprietyToSi(element.getProperties(), element.getPropertiesSymbols(), 2))).withColor(DyeColor.GRAY.getTextColor())) // properties
                    .append(Component.literal("[%s,%s]: ".formatted(element.getPinA() == null ? "gnd" : element.getPinA().address, element.getPinB() == null ? "gnd" : element.getPinB().address)).withColor(DyeColor.LIGHT_GRAY.getTextColor())) // mna pins
                    .append(Component.literal("%.2fv, %.2fA, %.2fW".formatted(element.getVoltageDifference(),element.getCurrent(),element.getPower())).withColor(DyeColor.LIME.getTextColor()));// electrical quantity
    }

    public static Consumer<ElectricalBE, ElectricalElementModule> SET_ELEMENT_IN_THE_WORLD(final Element element)
    {
        return (ElectricalElementModule module, ElectricalBE holder, Level level, BlockPos pos) -> {
            module.setElement(element);
        };
    }
}
