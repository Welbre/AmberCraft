package welbre.ambercraft.blocks.heat;

import welbre.ambercraft.module.heat.HeatModule;

public class CopperHeatConductorBlock extends HeatConductorBlock {
    public CopperHeatConductorBlock(Properties p) {
        super(p, 0.4f);
        moduleConstructor.push(HeatModule.SET_THERMAL_CONDUCTIVITY_CONSUMER(10.0));
    }
}
