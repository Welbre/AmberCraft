package welbre.ambercraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import welbre.ambercraft.Main;
import welbre.ambercraft.sim.heat.HeatNode;

public class HeatFurnaceBE extends HeatConductorBE {
    private int timer = 0;
    private int power = 1;
    private boolean isOn = false;

    public HeatFurnaceBE(BlockPos pos, BlockState blockState) {
        super(Main.BlockEntity.HEAT_FURNACE_BE.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (!level.isClientSide && blockEntity instanceof HeatFurnaceBE furnace) {
            if (furnace.isOn)
            {
                if (furnace.timer++ >= 5)
                {
                    HeatNode node = furnace.heatModule.getHeatNode();
                    if (node.getTemperature() < 100)
                        furnace.burnout();
                    if (node.getTemperature() < 1000)
                        node.transferHeat(furnace.power);
                    furnace.timer = 0;
                }
            }
        }
        HeatConductorBE.tick(level, pos, state, blockEntity);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        power = tag.getInt("power");
        isOn = tag.getBoolean("overcharged");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("power", power);
        tag.putBoolean("isOne", isOn);
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
    }
}
