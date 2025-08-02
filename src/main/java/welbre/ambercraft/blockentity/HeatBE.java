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
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.heat.HeatModule;

/// A ready to use BlockEntity with all {@link HeatModule} stuff set.<br>
/// Remember to add the block that will use this class in {@link AmberCraft.Blocks#HEAT_BE_USES}.
public class HeatBE extends BlockEntity implements ModulesHolder {
    protected HeatModule heatModule = new HeatModule();

    public HeatBE(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public HeatBE(BlockPos pos, BlockState state)
    {
        super(AmberCraft.BlockEntity.HEAT_CONDUCTOR_BE.get(), pos, state);
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
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        //super.handleUpdateTag(tag, lookupProvider);
        heatModule.ID = tag.getInt("id");
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        var tag= new CompoundTag();
        tag.putInt("id", heatModule.ID);
        return tag;
    }



    @Override
    public void onDataPacket(@NotNull Connection net, @NotNull ClientboundBlockEntityDataPacket pkt, HolderLookup.@NotNull Provider lookupProvider) {
        //super.onDataPacket(net, pkt, lookupProvider);
        heatModule.ID = pkt.getTag().getInt("id");
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        try
        {
            heatModule.refresh(this);
        } catch (Exception e)
        {
            AmberCraft.LOGGER.error("Error loading heat module for block entity at " + getBlockPos() + " with ID " + heatModule.ID, e);
            level.removeBlock(getBlockPos(), false);
        }
    }

    @Override
    public Module[] getModules() {
        return new Module[]{heatModule};
    }

    @Override
    public Module[] getModule(Direction direction) {
        return new Module[]{heatModule};
    }

    @Override
    public @NotNull Module[] getModule(Object object) {
        if (object instanceof Direction dir)
            return getModule(dir);
        return new Module[0];
    }

    public void setHeatModule(HeatModule module) {
        this.heatModule = module;
    }

    public HeatModule getHeatModule() {
        return heatModule;
    }

    public static void TICK(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity)
    {
        if (blockEntity instanceof ModulesHolder holder)
            holder.tickModules(level, pos, state, blockEntity);
    }
}
