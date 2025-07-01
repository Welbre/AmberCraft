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
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.Main;
import welbre.ambercraft.module.HeatModule;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.Module;

public class HeatSinkBlockEntity extends BlockEntity implements ModulesHolder {
    public HeatModule heatModule = new HeatModule(this){
        @Override
        public void tick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
            super.tick(level, pos, state, blockEntity);
            level.sendBlockUpdated(pos, state,state, 0);
        }
    };

    public HeatSinkBlockEntity(BlockPos pos, BlockState blockState) {
        super(Main.Tiles.HEAT_SINK_BLOCK_ENTITY.get(), pos, blockState);
        heatModule.setEnvThermalConductivity(2.0);
        heatModule.setThermalMass(10.0);
        heatModule.setThermalConductivity(100.0);
    }

    @Override
    public Module[] getModules() {
        return new Module[]{heatModule};
    }

    @Override
    public Module[] getModule(Direction direction) {
        if (direction == Direction.DOWN)
            return new Module[]{heatModule};
        return new Module[0];
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveData(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadData(tag,registries);
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

    public static <T extends BlockEntity> void TICK(Level level, BlockPos pos, BlockState state, T t) {
        if (t instanceof HeatSinkBlockEntity sink)
            sink.heatModule.tick(level, pos, state, sink);
    }
}
