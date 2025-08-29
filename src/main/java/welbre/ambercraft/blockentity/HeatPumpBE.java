package welbre.ambercraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blocks.heat.HeatPumpBlock;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.heat.HeatModule;

public class HeatPumpBE extends ModulesHolder {
    public HeatModule coldModule = new HeatModule();
    public HeatModule hotModule = new HeatModule();

    public double power = 0;

    public HeatPumpBE(BlockPos pos, BlockState blockState) {
        super(AmberCraft.BlockEntity.HEAT_PUMP_BE.get(), pos, blockState);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        power = tag.getDouble("power");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putDouble("power", power);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        var tag = super.getUpdateTag(registries);
        tag.putDouble("power", power);
        return tag;
    }

    @Override
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide)
        {
            coldModule.getHeatNode().transferHeat(-power);
            hotModule.getHeatNode().transferHeat(power);
        }
        super.tick(level, pos, state);
    }

    @Override
    public @NotNull Module[] getModules() {
        return new Module[]{coldModule, hotModule};
    }

    @Override
    public @NotNull Module[] getModule(Direction direction) {
        var face = getBlockState().getValue(HeatPumpBlock.FACING);
        if (direction == face)
            return new Module[]{hotModule};
        else if (direction == face.getOpposite())
            return new Module[]{coldModule};


        return new Module[0];
    }

    @Override
    public @NotNull Module[] getModule(Object object) {
        if (object instanceof Direction dir)
            return getModule(dir);
        return new Module[0];
    }

    public void setColdModule(HeatModule module) {
        coldModule = module;
    }

    public HeatModule getColdModule() {
        return coldModule;
    }

    public HeatModule getHotModule() {
        return hotModule;
    }

    public void setHotModule(HeatModule hotModule) {
        this.hotModule = hotModule;
    }
}
