package welbre.ambercraft.blocks.electrical;

import kuse.welbre.sim.electrical.elements.ACVoltageSource;

public class ACVoltageSourceBlock extends VoltageSourceBlock
{
    public ACVoltageSourceBlock(Properties p) {
        super(p);
        factory.setConstructor((module, entity, factory, level, pos) -> {
            module.setElement(new ACVoltageSource(0,1));
        });
    }
}
