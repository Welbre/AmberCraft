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
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.heat.HeatModule;

public class HeatConductorBE extends BlockEntity implements ModulesHolder {
    protected HeatModule heatModule = new HeatModule();

    public HeatConductorBE(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadData(tag,registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveData(tag, registries);
    }

    @Override
    public Module[] getModules() {
        return new Module[]{heatModule};
    }

    @Override
    public Module[] getModule(Direction direction) {
        return new Module[]{heatModule};
    }

    public void setHeatModule(HeatModule module) {
        this.heatModule = module;
    }

    public HeatModule getHeatModule() {
        return heatModule;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity)
    {
        if (blockEntity instanceof HeatConductorBE conductorBE)
            conductorBE.tickModules(level, pos, state, blockEntity);
    }
}
