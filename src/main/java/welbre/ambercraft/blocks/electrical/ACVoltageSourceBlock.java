package welbre.ambercraft.blocks.electrical;

import kuse.welbre.sim.electrical.elements.ACVoltageSource;
import welbre.ambercraft.module.electrical.ElectricalElementModule;

public class ACVoltageSourceBlock extends VoltageSourceBlock
{
    public ACVoltageSourceBlock(Properties p) {
        super(p);
        elementConstructor.push(ElectricalElementModule.SET_ELEMENT_IN_THE_WORLD(new ACVoltageSource(0,1)));
    }
}
