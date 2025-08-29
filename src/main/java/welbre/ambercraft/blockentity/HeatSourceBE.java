package welbre.ambercraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;

public class HeatSourceBE extends HeatBE {
    public double temperature;
    public double heat = 0;
    public Mode mode;

    public HeatSourceBE(BlockPos pos, BlockState state) {
        super(AmberCraft.BlockEntity.HEAT_SOURCE_BE.get(), pos, state);
        mode = Mode.TEMPERATURE;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putDouble("temperature", temperature);
        tag.putDouble("heat", heat);
        tag.putString("mode", mode.name());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        temperature = tag.getDouble("temperature");
        heat = tag.getDouble("heat");
        mode = Mode.valueOf(tag.getString("mode"));
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        var tag = super.getUpdateTag(registries);
        tag.putDouble("temperature", temperature);
        tag.putDouble("heat", heat);
        tag.putString("mode", mode.name());
        return tag;
    }

    @Override
    public void onDataPacket(@NotNull Connection net, @NotNull ClientboundBlockEntityDataPacket pkt, HolderLookup.@NotNull Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
        temperature = pkt.getTag().getDouble("temperature");
        heat = pkt.getTag().getDouble("heat");
        mode = Mode.valueOf(pkt.getTag().getString("mode"));
    }

    @Override
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide)
            if (this.mode == Mode.TEMPERATURE)
                this.heatModule.getHeatNode().setTemperature(this.temperature);
            else if (this.mode == Mode.HEAT)
                this.heatModule.getHeatNode().transferHeat(this.heat);

        super.tick(level, pos, state);
    }


    public enum Mode {
        TEMPERATURE,
        HEAT
    }
}
