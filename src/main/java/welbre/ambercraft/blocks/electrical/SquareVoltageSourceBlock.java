package welbre.ambercraft.blocks.electrical;

import kuse.welbre.sim.electrical.elements.SquareVoltageSource;

public class SquareVoltageSourceBlock extends VoltageSourceBlock
{
    public SquareVoltageSourceBlock(Properties p) {
        super(p);
        factory.setConstructor((module, entity, factory, level, pos) -> {
            module.setElement(new SquareVoltageSource(0,1));
        });
    }
}
