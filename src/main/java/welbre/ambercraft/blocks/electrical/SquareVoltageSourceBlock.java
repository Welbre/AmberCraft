package welbre.ambercraft.blocks.electrical;

import kuse.welbre.sim.electrical.elements.SquareVoltageSource;
import welbre.ambercraft.module.electrical.ElectricalElementModule;

public class SquareVoltageSourceBlock extends VoltageSourceBlock
{
    public SquareVoltageSourceBlock(Properties p) {
        super(p);
        elementConstructor.push(ElectricalElementModule.SET_ELEMENT_IN_THE_WORLD(new SquareVoltageSource(0,1)));
    }
}
