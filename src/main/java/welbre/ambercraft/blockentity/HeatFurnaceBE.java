package welbre.ambercraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.sim.heat.HeatNode;

public class HeatFurnaceBE extends HeatBE {
    private int power = 1;
    private boolean isOn = false;

    public HeatFurnaceBE(BlockPos pos, BlockState blockState) {
        super(AmberCraft.BlockEntity.HEAT_FURNACE_BE.get(), pos, blockState);
    }

    @Override
    public void tick(Level level, BlockPos pos, BlockState state) {
        super.tick(level, pos, state);
        if (!level.isClientSide)
        {
            if (this.isOn)
            {
                HeatNode node = this.heatModule.getHeatNode();
                if (node.getTemperature() < 100)
                    this.burnout();
                if (node.getTemperature() < 1000)
                    node.transferHeat(this.power / 5.0);
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        power = tag.getInt("power");
        isOn = tag.getBoolean("isOne");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("power", power);
        tag.putBoolean("isOne", isOn);
    }

    @Override
    public void onDataPacket(@NotNull Connection net, @NotNull ClientboundBlockEntityDataPacket pkt, HolderLookup.@NotNull Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
        power = pkt.getTag().getInt("power");
        isOn = pkt.getTag().getBoolean("isOn");
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("power", power);
        tag.putBoolean("isOn", isOn);
        return tag;
    }

    public void burnout() {
        isOn = false;
        if (level != null)
        {
            level.playSound(null, getBlockPos(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
            level.setBlock(getBlockPos(), level.getBlockState(getBlockPos()).setValue(FurnaceBlock.LIT, false), FurnaceBlock.UPDATE_ALL);
        }
        setChanged();
    }

    public void ignite(){
        isOn = true;
        getHeatModule().getHeatNode().setTemperature(Math.max(getHeatModule().getHeatNode().getTemperature() + 100, 150));
        if (level != null)
        {
            level.playSound(null, getBlockPos(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
            level.setBlock(getBlockPos(), level.getBlockState(getBlockPos()).setValue(FurnaceBlock.LIT, true), FurnaceBlock.UPDATE_ALL);
        }
        setChanged();
    }

    public void addPower() {
        power +=10;
        setChanged();
        if (level != null)
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    public double getPower() {
        return power;
    }
}
