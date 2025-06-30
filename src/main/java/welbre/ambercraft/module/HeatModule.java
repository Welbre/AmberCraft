package welbre.ambercraft.module;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class HeatModule implements Module {

    public static final double DEFAULT_TIME_STEP = 0.05;

    protected double temperature = 0;
    protected double thermal_mass = 1.0;
    protected double thermal_conductivity = 1.0;
    protected double envTemperature = Double.MIN_NORMAL;
    protected double envConductive = 0;

    public HeatModule() {
    }

    public HeatModule(BlockEntity entity) {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            this.envTemperature = getAmbientTemperature(level, entity.getBlockPos());
            this.temperature = this.envTemperature;
        }
    }

    public void transferHeatToNeighbor(Level level, BlockPos pos){
        for (Direction d : Direction.values()) {
            BlockEntity entity = level.getBlockEntity(pos.relative(d));
            if (entity instanceof ModularBlockEntity modular)
            {
                HeatModule[] modules = modular.getModule(HeatModule.class, d.getOpposite());
                if (modules != null)
                    for (HeatModule module : modules)
                        this.transferHeat(module, DEFAULT_TIME_STEP);
            }
        }
    }

    public void transferHeat(HeatModule target, double dt){
        if (target.temperature > this.temperature)//can only transfer energy from this to target
            return;
        double resistence = (1.0/thermal_conductivity) + (1.0/ target.thermal_conductivity);
        double power, heat;

        double step = dt;
        while (dt > 0 && this.temperature != target.temperature)
        {
            power = (this.temperature - target.temperature) / resistence;
            heat = power * step;
            if //check if the corp contains half of energy transited.
            (
                    heat < (this.temperature - target.temperature) / 2.0
            ) {
                this.temperature -= heat / thermal_mass;
                target.temperature += heat / thermal_mass;
                dt -= step;
                step = resistence / 2.01;
            } else {
                //todo check if this works in a environment with different heat capacity
                step = resistence / 2.01;
                //step = (this.temperature - target.temperature) / (2.01 * power);
            }
        }
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
        this.envConductive = conductive;
        this.envTemperature = temperature;
    }

    public void setEnvThermalConductivity(double conductive) {
        this.envConductive = conductive;
    }

    public void setThermalMass(double thermal_mass) {
        this.thermal_mass = thermal_mass;
    }

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

    @Override
    public void writeData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putDouble("th_temp", temperature);
        tag.putDouble("th_mass", thermal_mass);
        tag.putDouble("th_cond", thermal_conductivity);
        tag.putDouble("env_temp", envTemperature);
    }

    @Override
    public void readData(CompoundTag tag, HolderLookup.Provider registries) {
        temperature = tag.getDouble("th_temp");
        thermal_mass = tag.getDouble("th_mass");
        thermal_conductivity = tag.getDouble("th_cond");
        envTemperature = tag.getDouble("env_temp");
    }

    @Override
    public void tick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        transferHeatToNeighbor(level,pos);
        if (this.envConductive != 0)
            transferHeatToEnvironment(envTemperature, envConductive, DEFAULT_TIME_STEP);
    }

    public static double getAmbientTemperature(Level level, BlockPos pos){
        Holder<Biome> biome = level.getBiomeManager().getBiome(pos);
        float baseTemperature = biome.value().getBaseTemperature();
        return Math.min(baseTemperature,1.0f) * 40f;
    }
}
