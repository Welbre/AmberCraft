package welbre.ambercraft.module.electrical;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.elements.Resistor;
import kuse.welbre.tools.Tools;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.profiling.Profiler;
import org.jetbrains.annotations.NotNull;
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
 * between the {@link #pin} and the other pin.</p>
 * <p>The {@link #resistence} is the cable resistence in ohm, the value in used to create the resistor.<br>The resistence in a cable->cable connection is the average resistence
 * of the cables, and a cable->pin connection uses half of the cable resistence. <i>A simple way to visualize this is, "the distance between the cable center and the border is 1/2 block",
 * so a cable-cable is half cable + half cable or (r0 + r1) / 2, and the cable-pin is a cable that goes from the center to the boarder or (r) / 2</i></p>
 */
public class ElectricalCableModule extends NetworkModule implements DebugToolInfo {
    public Circuit.Pin pin = new Circuit.Pin();
    private @NotNull Resistor[] resistors = {};
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
    public void connect(NetworkModule target) {
        //cable -> pin connection
        //creates a resistor with half-cable resistence
        if (target instanceof ElectricalPinModule epm)
        {
            addResistor(new Resistor(pin, epm.getPin(), resistence / 2));
            super.connect(epm.getElectricalModule());//connect to the elementModule instead of the pin!
        }
        // cable -> cable connection
        // computes the average between the pins resistence and creates a resistor to connect the pins.
        else if (target instanceof ElectricalCableModule ecm)
        {
            addResistor(new Resistor(pin, ecm.pin, (resistence + ecm.resistence) / 2));
            super.connect(target);
        } else {
            super.connect(target);
        }
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

    public void addResistor(Resistor r)
    {
        resistors = Arrays.copyOf(resistors, resistors.length + 1);
        resistors[resistors.length - 1] = r;
    }

    /// Returns a copy of the resistors
    public Resistor[] getResistors() {
        return Arrays.copyOf(resistors, resistors.length);
    }

    @Override
    public List<Component> getInfo() {
        List<Component> list = new ArrayList<>();
        list.add(Component.literal("Pin: " + pin.address));
        list.add(Component.literal("Resistence: " + Tools.proprietyToSi(resistence, "Ω")));

        for (Resistor r : resistors)
            list.add(ElectricalModule.GET_ELEMENT_INFO(r));

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
