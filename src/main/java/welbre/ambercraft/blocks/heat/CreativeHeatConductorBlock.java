package welbre.ambercraft.blocks.heat;

public class CreativeHeatConductorBlock extends HeatConductorBlock{
    public CreativeHeatConductorBlock(Properties p) {
        super(p, 0.25f);
        moduleConstructor.push((module, holder, level, pos) -> module.getHeatNode().setThermalConductivity(50.0));
    }
}
