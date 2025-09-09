package welbre.ambercraft.module.electrical;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.elements.Resistor;
import kuse.welbre.tools.Tools;
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

public class ElectricalCableModule extends NetworkModule implements DebugToolInfo {
    public Circuit.Pin pin = new Circuit.Pin();
    private @NotNull Resistor[] resistors = {};
    public final double resistence;

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
            super.connect(epm.getElectricalModule());//connect to the elementModule instead the pin!
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
    public Master createMaster() {
        return new ElectricalModulesMaster(this);
    }

    @Override
    public void tick(ModulesHolder entity) {
        if (!this.isMaster())
            return;
        Profiler.get().push("ElectricalCableModule tick");

        master.tick(entity);

        Profiler.get().pop();
    }

    public void addResistor(Resistor r)
    {
        resistors = Arrays.copyOf(resistors, resistors.length + 1);
        resistors[resistors.length - 1] = r;
    }

    @Override
    public List<Component> getInfo() {
        List<Component> list = new ArrayList<>();
        list.add(Component.literal("Resistence: " + Tools.proprietyToSi(resistence, "Ω")));

        for (Resistor r : resistors)
            list.add(ElectricalModule.GET_ELEMENT_INFO(r));

        return list;
    }

    @Override
    public <T extends Module> ModuleType<T> getType() {
        return (ModuleType<T>) AmberCraft.ModuleTypes.ELECTRICAL_CABLE_MODULE_TYPE.get();
    }
}
