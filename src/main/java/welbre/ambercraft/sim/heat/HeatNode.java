package welbre.ambercraft.sim.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import welbre.ambercraft.sim.Node;

import java.io.Serializable;

public class HeatNode extends Node implements Serializable {
    public static final double DEFAULT_TIME_STEP = 0.05;

    protected double temperature = 0;
    protected double thermal_mass = 1.0;
    protected double thermal_conductivity = 1.0;
    protected double envTemperature = 0;
    protected double envConductivity = 0;

    public HeatNode() {
    }

    public HeatNode(double temperature) {
        this.temperature = temperature;
    }

    private void transferHeatToChildren(Node[] nodes)
    {
        for (Node node : nodes)
            if (node instanceof HeatNode heatNode)
                this.transferHeat(heatNode, DEFAULT_TIME_STEP);
    }

    /**
     * Use to transfer heat between this node and the environment.<br>
     * This method is used internally to simulate a constant heat transfer with the environment using the {@link HeatNode#envTemperature} and {@link HeatNode#envConductivity},
     * if you want to simulate an ambient heat lost, modify this fields, only use this method to move heat temporarily like throw water in hot stuff.
     * @param env_temperature the environment temperature.
     * @param env_conductivity how good the environment is to transfer heat.
     * @param dt the time to simulate the heat transfer.
     */
    public void transferHeatToEnvironment(double env_temperature, double env_conductivity, double dt){
        double resistence = (1.0/thermal_conductivity) + (1.0/env_conductivity);
        double power, heat;

        double step = dt;
        while (dt > 0 && this.temperature != env_temperature)
        {
            power = (this.temperature - env_temperature) / resistence;
            heat = power * step;
            if //check if the corp contains half of energy transited.
            (
                    Math.abs(heat) < Math.abs(this.temperature - env_temperature) / 2.0
            ) {
                this.temperature -= heat / thermal_mass;
                dt -= step;
                step = resistence / 2.01;
            } else {
                //todo check if this works in a environment with different heat capacity
                step = resistence / 2.01;
            }
        }
    }

    ///This method will be re-implemented to a better simulation.
    @Deprecated
    public void transferHeat(HeatNode target, double dt)
    {
        //todo need to implement a fast way to simulate this, pre-calculating the power for each connection, and only updating the temperature after all power be calculated.
        double resistance = (1.0/thermal_conductivity) + (1.0/ target.thermal_conductivity);
        double teq = (temperature * thermal_mass + target.temperature * target.thermal_mass) / (thermal_mass + target.thermal_mass);
        double tau = resistance*(this.thermal_mass*target.thermal_mass)/(this.thermal_mass + target.thermal_mass);

        // Calculate new temperatures for both nodes based on equilibrium temperature and time constant
        double t1 = teq + (this.temperature - teq) * Math.pow(Math.E, -dt / tau);
        double t2 = teq + (target.temperature - teq) * Math.pow(Math.E, -dt / tau);

        this.temperature = t1;
        target.temperature = t2;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();

        var heatTag = new CompoundTag();
        heatTag.putDouble("temp", temperature);
        heatTag.putDouble("t_m", thermal_mass);
        heatTag.putDouble("t_c", thermal_conductivity);
        heatTag.putDouble("e_c", envConductivity);
        heatTag.putDouble("e_t", envTemperature);
        tag.put("heat_tag",heatTag);
        return tag;
    }

    @Override
    public Node fromTag(CompoundTag tag) {
        HeatNode node = (HeatNode) super.fromTag(tag);

        var heatTag = tag.getCompound("heat_tag");
        temperature = heatTag.getDouble("temp");
        thermal_mass = heatTag.getDouble("t_m");
        thermal_conductivity = heatTag.getDouble("t_c");
        envConductivity = heatTag.getDouble("e_c");
        envTemperature = heatTag.getDouble("e_t");
        return node;
    }

    public void run(Node[] nodes) {
        transferHeatToChildren(nodes);
        if (this.envConductivity > 0)
            transferHeatToEnvironment(envTemperature, envConductivity, DEFAULT_TIME_STEP);
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public void transferHeat(double heat){
        temperature += heat / thermal_mass;
    }

    public double getThermal_conductivity() {
        return thermal_conductivity;
    }

    public void setThermalConductivity(double thermal_conductivity) {
        this.thermal_conductivity = thermal_conductivity;
    }

    public void setEnvConditions(double temperature, double conductive){
        this.envConductivity = conductive;
        this.envTemperature = temperature;
    }

    public void setEnvThermalConductivity(double conductive) {
        this.envConductivity = conductive;
    }

    public void setThermalMass(double thermal_mass) {
        this.thermal_mass = thermal_mass;
    }

    public static double GET_AMBIENT_TEMPERATURE(LevelAccessor level, BlockPos pos){
        if (level == null)
            return 0;
        Holder<Biome> biome = level.getBiomeManager().getBiome(pos);
        float baseTemperature = biome.value().getBaseTemperature();
        return Math.min(baseTemperature,1.0f) * 40f;
    }
}
