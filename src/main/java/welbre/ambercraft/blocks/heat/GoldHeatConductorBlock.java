package welbre.ambercraft.blocks.heat;


public class GoldHeatConductorBlock extends HeatConductorBlock{
    public GoldHeatConductorBlock(Properties p) {
        super(p, 0.3f);
        moduleConstructor.push((module, holder, level, pos) -> module.getHeatNode().setThermalConductivity(7.98));
    }
}
