package welbre.ambercraft.module.electrical;

import kuse.welbre.sim.electrical.Circuit;
import net.minecraft.core.Direction;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.ModuleType;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.network.Master;
import welbre.ambercraft.module.network.NetworkModule;

/**
 * This module represents a pin in the circuit, is used in combination with {@link ElectricalElementModule} to connect different pins in the element.<br>
 * <h3>Practical example</h3>
 * {@link welbre.ambercraft.blockentity.electrical.DirectionalElectricalBE DirectionalElectricalBE} returns different terminal modules depending on the face,
 * after the connection is done, in the compilation face the {@link ElectricalElementModule#compile()} uses the terminal module returned by the BE that
 * is stored in {@link ElectricalElementModule#terminalA} and {@link ElectricalElementModule#terminalB} to set the {@link ElectricalElementModule#element elemet} pins.<br>
 * <h3>How to use</h3>
 * <p>
 *     Create a {@link ElectricalElementModule} and return the element module and him terminals it in you {@link ModulesHolder},
 *     use {@link ElectricalElementModule#getTerminalA()} and {@link ElectricalElementModule#getTerminalB()} to access the terminal modules.
 * </p>
 * <p>
 *     In the {@link ModulesHolder#getModule(Direction)} you can send different terminals depending on how you desire that the block work.
 * </p>
 *
 * <i>
 *     See: {@link welbre.ambercraft.blockentity.electrical.ElectricalBE ElectricalBE} and {@link welbre.ambercraft.blockentity.electrical.DirectionalElectricalBE DirectionalElectricalBE}
 *     to see a practical example.
 * </i>
 */
public class ElectricalTerminalModule extends NetworkModule {
    protected final ElectricalModule electrical;
    protected Circuit.Pin[] terminal = {new Circuit.Pin()};

    public ElectricalTerminalModule(ElectricalModule electrical)
    {
        this.electrical = electrical;
    }

    @Override
    public boolean connect(NetworkModule target)
    {
        //Set the target pin as this pin.
        //Therefore, in the compilation step they have the same pin and connect currency in the MNA
        if (target instanceof ElectricalTerminalModule etm)
            etm.terminal = this.terminal;

        return super.connect(target);
    }

    @Override
    public void disconnectAll()
    {
        super.disconnectAll();
        terminal = new Circuit.Pin[]{new Circuit.Pin()};
    }

    public ElectricalModule getElectrical() {
        return electrical;
    }

    public Circuit.Pin[] getTerminal() {
        return terminal;
    }

    @Override
    public void onLoad(ModulesHolder entity)
    {

    }

    @Override
    public Master createMaster() {
        return new ElectricalModulesMaster(this);//used only to avoid to crash
    }

    @Override
    public void tick(ModulesHolder entity)
    {
        if (!this.isMaster())
            return;
        master.tick(entity);
    }

    @Override
    public ModuleType<?> getType() {
        return AmberCraft.ModuleTypes.ELECTRICAL_TERMINAL_MODULE_TYPE.get();
    }
}
