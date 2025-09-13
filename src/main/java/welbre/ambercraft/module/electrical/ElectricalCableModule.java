package welbre.ambercraft.module.electrical;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.elements.Resistor;
import kuse.welbre.tools.Tools;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.DebugToolInfo;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModuleType;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.network.Master;
import welbre.ambercraft.module.network.NetworkModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <h5>This module is used to create electrical cables.</h5>
 * <p>The cable uses a simple model, only a resistor connected in series between 2 pins, without capacitance, ground fault, and other real world cables effects.</p>
 * <p>The resistors are created on demand, when some method calls {@link #connect(NetworkModule)} the super method is called connecting both networkModules and creating a new resistor
 * between the {@link #terminal} and the other pin.</p>
 * <p>The {@link #resistence} is the cable resistence in ohm, the value in used to create the resistor.<br>The resistence in a cable->cable connection is the average resistence
 * of the cables, and a cable->pin connection uses half of the cable resistence. <i>A simple way to visualize this is, "the distance between the cable center and the border is 1/2 block",
 * so a cable-cable is half cable + half cable or (r0 + r1) / 2, and the cable-pin is a cable that goes from the center to the boarder or (r) / 2</i></p>
 */
public class ElectricalCableModule extends ElectricalModule implements DebugToolInfo {
    protected Circuit.Pin[] terminal = {new Circuit.Pin()};
    protected Circuit.Pin[][] terminalEnd = {};
    protected Resistor[] resistors = {};
    /// Resistence in ohms.
    /// @see ElectricalCableModule
    private double resistence;

    public ElectricalCableModule() {
        resistence = 0;
    }

    public ElectricalCableModule(double resistence) {
        this.resistence = resistence;
    }

    @Override
    public boolean connect(NetworkModule target) {
        //cable -> pin connection
        //creates a resistor with half-cable resistence
        if (target instanceof ElectricalTerminalModule epm)
        {
            if (super.connect(epm.electrical))//connect to the elementModule instead of the pin!
            {
                addResistor(epm.terminal, resistence / 2);//only create a new resistor if a new connection has been created.
                return true;
            }
            return false;
        }
        // cable -> cable connection
        // computes the average between the pins resistence and creates a resistor to connect the pins.
        else if (target instanceof ElectricalCableModule ecm)
        {
            if (super.connect(target))
            {
                addResistor(ecm.terminal, (resistence + ecm.resistence) / 2);
                return true;
            }
            return false;
        } else {
            return super.connect(target);
        }
    }

    @Override
    public void disconnectAll() {
        List<NetworkModule> list = new ArrayList<>(List.of(neighbors));
        if (root != null)
            list.add(root);

        for (NetworkModule child : list)
        {
            //cable -> cable disconnection
            if (child instanceof ElectricalCableModule ecm)
            {
                ecm.removeResistorsWithPin(this.terminal);
            }
            //element -> cable disconnection
            //notice that in the connection fase, an ElectricalPinModule is connected, but they are a wrapper
            //in the end a correspondent ElectricalModule is connected, and we don't know at each pin they have connected before.
            //so we try to disconnect both pins, but it isn't a good idea because the null represents the ground and can be connected in
            //any other resistor, causing a remotion of random resistors. at this point we can't prevent this from happening,
            //but throw an exception to notify the devs and help with the debug.
            else if (child instanceof ElectricalElementModule em)
            {
                int l = resistors.length;
                removeResistorsWithPin(em.getTerminalA().terminal);
                int l0 = resistors.length;
                removeResistorsWithPin(em.getTerminalB().terminal);
                if (l != l0 && l0 != resistors.length)
                    AmberCraft.LOGGER.warn("Disconnected from both pins at same time, possible fault.", new IllegalStateException("") );
            }
        }
        resistors = new Resistor[0];
        terminalEnd = new Circuit.Pin[0][0];
        super.disconnectAll();
    }

    @Override
    public void alloc() {

    }

    @Override
    public Element[] compile() {
        for (int i = 0; i < terminalEnd.length; i++)
            resistors[i].connect(terminal[0], terminalEnd[i][0]);

        return resistors;
    }

    @Override
    public void free() {

    }

    @Override
    public void writeData(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeData(tag, registries);
        tag.putDouble("resistence", resistence);
    }

    @Override
    public void readData(CompoundTag tag, HolderLookup.Provider registries) {
        super.readData(tag, registries);
        resistence = tag.getDouble("resistence");
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

    protected void addResistor(Circuit.Pin[] end, double resistence)
    {
        resistors = Arrays.copyOf(resistors, resistors.length + 1);
        resistors[resistors.length - 1] = new Resistor(resistence);
        terminalEnd = Arrays.copyOf(terminalEnd, terminalEnd.length + 1);
        terminalEnd[terminalEnd.length - 1] = end;
    }

    /// looks in the resistor array and remove all resistors that are connected to <code>ptr</code>.
    /// @param ptr pin to remove
    protected void removeResistorsWithPin(Circuit.Pin[] ptr)
    {
        List<Integer> trm = new ArrayList<>();
        for (int i = 0; i < terminalEnd.length; i++)//mark all resistors to be removed
            if (terminalEnd[i] == ptr)
                trm.add(i);

        if (trm.isEmpty())
            return;
        final int length = terminalEnd.length - trm.size();
        if (length == 0)
        {
            terminalEnd = new Circuit.Pin[0][0];{}
            resistors = new Resistor[0];
            return;
        }
        Circuit.Pin[][] temp0 = new Circuit.Pin[length][1];
        Resistor[] temp1 = new Resistor[length];
        int j = 0;
        for (int i = 0; i < terminalEnd.length; i++)
        {
            if (!trm.contains(i))
            {
                temp0[j] = terminalEnd[i];
                temp1[j] = resistors[i];
                j++;
            }
        }
        terminalEnd = temp0;
        resistors = temp1;
    }

    /// Returns a copy of the resistors
    public Resistor[] getResistors() {
        return Arrays.copyOf(resistors, resistors.length);
    }

    @Override
    public List<Component> getInfo() {
        List<Component> list = new ArrayList<>();
        list.add(Component.literal("Pin: %s, Voltage: %s".formatted(
                terminal[0].address,
                terminal[0].P_voltage != null ? Tools.proprietyToSi(terminal[0].P_voltage[0], "V") : "NaN"
        )));
        list.add(Component.literal("Resistence: " + Tools.proprietyToSi(resistence, "Ω")));

        for (Resistor r : resistors)
            list.add(ElectricalElementModule.GET_ELEMENT_INFO(r));

        return list;
    }

    @Override
    public <T extends Module> ModuleType<T> getType() {
        return (ModuleType<T>) AmberCraft.ModuleTypes.ELECTRICAL_CABLE_MODULE_TYPE.get();
    }

    public double getResistence() {
        return resistence;
    }
}
