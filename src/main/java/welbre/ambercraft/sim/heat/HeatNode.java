package welbre.ambercraft.sim.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import welbre.ambercraft.sim.Node;
import welbre.ambercraft.sim.VersionHandler;

import java.io.Serializable;

public class HeatNode extends Node implements Serializable {
    public static final double DEFAULT_TIME_STEP = 0.05;

    private double temperature = 0;//ºC
    private double thermal_mass = 1.0;// j / ºC
    private double thermal_resistence = 1.0;// ºC / W
    private double envTemperature = 0;// ºC
    private double env_resistence = 0;// W / ºC
    private double deltaTemp = 0;//used to a more precise simulation, first compute all heat that will be transfer in this node, and after update the temperature.

    public HeatNode() {
    }

    public HeatNode(double temperature) {
        this.temperature = temperature;
    }

    public void computeSoftHeatWithChildren(Node[] nodes)
    {
        for (Node node : nodes)
            if (node instanceof HeatNode heatNode)
                this.transferHeat(heatNode, DEFAULT_TIME_STEP);
    }

    public void computeSoftHeatToEnvironment(){
        if (this.env_resistence > 0)
            computeSoftHeatToEnvironment(envTemperature, env_resistence, DEFAULT_TIME_STEP);
    }

    /**
     * Use to transfer heat between this node and the environment.<br>
     * This method is used internally to simulate constant heat transfer with the environment using the {@link HeatNode#envTemperature} and {@link HeatNode#env_resistence}.<br>
     * The heat transfer is a bit different from {@link HeatNode#transferHeat}, this method takes into account that the environment is infinitely big, therefore, the temperature is constant.
     * @param env_temperature the environment temperature.
     * @param env_resistence how resistant the environment is to transfer heat.
     * @param dt the time to simulate the heat transfer.
     */
    public void computeSoftHeatToEnvironment(double env_temperature, double env_resistence, double dt){
        double resistance = thermal_resistence + env_resistence;
        double teq = env_temperature;
        double itau = 1.0 / resistance * thermal_mass;

        double t = teq + (this.temperature - teq) * Math.pow(Math.E, -dt * itau);

        deltaTemp += (t - this.temperature);
    }

    ///This method will be re-implemented to a better simulation.
    @Deprecated
    public void transferHeat(HeatNode target, double dt)
    {
        double resistance = this.thermal_resistence +target.thermal_resistence;
        double teq = (temperature * thermal_mass + target.temperature * target.thermal_mass) / (thermal_mass + target.thermal_mass);
        double itau = (this.thermal_mass + target.thermal_mass) / (resistance*this.thermal_mass*target.thermal_mass);

        // Calculate new temperatures for both nodes based on equilibrium temperature and time constant
        double t1 = teq + (this.temperature - teq) * Math.pow(Math.E, -dt * itau);
        double t2 = teq + (target.temperature - teq) * Math.pow(Math.E, -dt * itau);

        this.deltaTemp += (t1 - this.temperature);
        target.deltaTemp += (t2 - target.temperature);
    }

    public void updateSoftHeat()
    {
        this.temperature += this.deltaTemp;
        this.deltaTemp = 0;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();

        var heatTag = new CompoundTag();
        VERSION_HANDLER.handleWrite(heatTag);
        heatTag.putDouble("temp", temperature);
        heatTag.putDouble("t_m", thermal_mass);
        heatTag.putDouble("t_r", thermal_resistence);
        heatTag.putDouble("e_r", env_resistence);
        heatTag.putDouble("e_t", envTemperature);
        heatTag.putDouble("d_t", deltaTemp);
        tag.put("heat_tag",heatTag);
        return tag;
    }

    @Override
    public Node fromTag(CompoundTag tag) {
        HeatNode node = (HeatNode) super.fromTag(tag);


        var heatTag = tag.getCompound("heat_tag");
        VERSION_HANDLER.handleRead(heatTag);

        temperature = heatTag.getDouble("temp");
        thermal_mass = heatTag.getDouble("t_m");
        thermal_resistence = heatTag.getDouble("t_r");
        env_resistence = heatTag.getDouble("e_r");
        envTemperature = heatTag.getDouble("e_t");
        deltaTemp = heatTag.getDouble("d_t");
        return node;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public void transferHeat(double heat){
        deltaTemp += heat / this.thermal_mass;
    }

    public double getThermalConductivity() {
        return 1.0 / thermal_resistence;
    }

    public void setThermalConductivity(double thermal_conductivity) {
        this.thermal_resistence = 1.0 / thermal_conductivity;
    }

    public void setEnvConditions(double temperature, double conductive){
        this.env_resistence = 1.0 / conductive;
        this.envTemperature = temperature;
    }

    public void setEnvThermalConductivity(double conductive) {
        this.env_resistence = 1.0 / conductive;
    }

    public void setThermalMass(double thermal_mass) {
        this.thermal_mass = thermal_mass;
    }

    public double getThermalMass() {
        return thermal_mass;
    }

    public double getEnvTemperature() {
        return envTemperature;
    }

    public double getEnvConductivity(){
        return 1.0 / env_resistence;
    }

    public double getEnvResistence() {
        return env_resistence;
    }

    public static double GET_AMBIENT_TEMPERATURE(LevelAccessor level, BlockPos pos){
        if (level == null)
            return 0;
        Holder<Biome> biome = level.getBiomeManager().getBiome(pos);
        float baseTemperature = biome.value().getBaseTemperature();
        return Math.min(baseTemperature,1.0f) * 40f;
    }

    private static final VersionHandler VERSION_HANDLER = VersionHandler.builder()
            .initVersion("performance", false)
            .upConvert(tag -> {
                double tc = tag.getDouble("t_c");
                double ec = tag.getDouble("e_c");
                tag.remove("t_c");
                tag.remove("e_c");
                tag.putDouble("t_r", 1.0 / tc);
                tag.putDouble("e_r", ec == 0 ? 0 : 1.0 / ec);//negative value if can't transfer heat to the environment.
                tag.putDouble("d_t",0);
            })
            .downConvert(tag -> {
                double tr = tag.getDouble("t_r");
                double er = tag.getDouble("e_r");
                tag.remove("e_r");
                tag.remove("t_r");
                tag.remove("d_t");
                tag.putDouble("t_c", 1.0 / tr);
                tag.putDouble("e_c", er == 0 ? 0 : 1.0 / er);//if negative it can't transfer heat to the environment, therefore, put 0 as conductance
            })
            .build()
            .build();

}
