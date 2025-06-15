package welbre.ambercraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class HeatBlockEntity extends BlockEntity {
    public static final double DEFAULT_TIME_STEP = 0.05;

    protected double temperature = 0;
    protected double thermal_mass = 1.0;
    protected double thermal_conductivity = 1.0;

    public HeatBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        temperature = tag.getDouble("th_temp");
        thermal_mass = tag.getDouble("th_mass");
        thermal_conductivity = tag.getDouble("th_cond");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putDouble("th_temp", temperature);
        tag.putDouble("th_mass", thermal_mass);
        tag.putDouble("th_cond", thermal_conductivity);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        // The packet uses the CompoundTag returned by #getUpdateTag. An alternative overload of #create exists
        // that allows you to specify a custom update tag, including the ability to omit data the client might not need.
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
    }

    public void transferHeatToNeighbor(Level level, BlockPos pos){
        for (Direction d : Direction.values()) {
            BlockEntity entity = level.getBlockEntity(pos.relative(d));
            if (entity instanceof HeatBlockEntity heatTile) {
                this.transferHeat(heatTile, DEFAULT_TIME_STEP);
            }
        }
    }

    public void transferHeat(HeatBlockEntity target, double dt){
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

    public void transferHeat(double heat){
        temperature += heat / thermal_mass;
    }

    public void transferHeatToEnvironment(double env_temperature,double env_conductivity, double dt){
        double resistence = (1.0/thermal_conductivity) + (1.0/env_conductivity);
        double power, heat;

        double step = dt;
        while (dt > 0 && this.temperature != env_temperature)
        {
            power = (this.temperature - env_temperature) / resistence;
            heat = power * step;
            if //check if the corp contains half of energy transited.
            (
                    heat < (this.temperature - env_temperature) / 2.0
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
}
