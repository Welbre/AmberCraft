package welbre.ambercraft.blocks.heat;

public class IronHeatConductorBlock extends HeatConductorBlock {
    public IronHeatConductorBlock(Properties p) {
        super(p, 0.45f);
        moduleConstructor.push((module, holder, level, pos) -> module.getHeatNode().setThermalConductivity(2.2));
    }
}
