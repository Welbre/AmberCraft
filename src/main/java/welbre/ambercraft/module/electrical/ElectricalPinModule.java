package welbre.ambercraft.module.electrical;

import kuse.welbre.sim.electrical.Circuit;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModuleType;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.network.Master;
import welbre.ambercraft.module.network.NetworkModule;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This class is a warper to the {@link ElectricalPinModule}, don't register this class!<br>
 */
public class ElectricalPinModule extends NetworkModule {
    private final ElectricalModule element;
    /// Gets the pin from the element.
    private transient final Supplier<Circuit.Pin> getter;
    /// Set the pin in the element.
    private transient final Consumer<Circuit.Pin> setter;

    public ElectricalPinModule() {
        this(null, null, null);
    }

    public ElectricalPinModule(ElectricalModule element, Supplier<Circuit.Pin> getter, Consumer<Circuit.Pin> setter) {
        this.element = element;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public void connect(NetworkModule target) {
        //pin -> pin
        //Basically set the pin in the other element to the pin in this element, so the 2 elements will be connected in the same pin
        if (target instanceof ElectricalPinModule other)
        {
            other.setter.accept(this.getter.get());
            element.connect(target);
        }
        // pin -> cable
        // delegates the task to the ElectricalCableModule, duo the need for create a resistor.
        else if (target instanceof ElectricalCableModule ecm)
            ecm.connect(this);
        else
            super.connect(target);
    }

    public Circuit.Pin getPin() {
        return getter.get();
    }

    ElectricalModule getElectricalModule() {
        return element;
    }

    @Override
    public void onLoad(ModulesHolder entity) {

    }

    @Override
    public Master createMaster() {
        return new ElectricalModulesMaster(this);//used only to avoid crash
    }

    @Override
    public void tick(ModulesHolder entity) {
        throw new UnsupportedOperationException("ElectricalPinModule is not tickable!");
    }

    @Override
    public <T extends Module> ModuleType<T> getType() {
        throw new UnsupportedOperationException("ElectricalPinModule don't have a registered type!");
    }
}
