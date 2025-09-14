package welbre.ambercraft.blockentity.electrical;

import kuse.welbre.sim.electrical.Circuit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blocks.electrical.GroundBlock;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.electrical.ElectricalCableModule;

public class GroundBE extends ModulesHolder {
    public GroundModule groundModule = new GroundModule(0.005);

    public GroundBE(BlockPos pos, BlockState state) {
        super(AmberCraft.BlockEntity.GROUND_BE.get(), pos, state);
    }

    @Override
    public @NotNull Module[] getModules() {
        return new Module[]{groundModule};
    }

    @Override
    public @NotNull Module[] getModule(Direction direction) {
        Direction value = getBlockState().getValue(GroundBlock.FACING);
        if (value == direction)
            return new Module[]{this.groundModule};
        return new Module[0];
    }

    public GroundModule getGroundModule() {
        return groundModule;
    }

    public void setGroundModule(GroundModule groundModule) {
        this.groundModule = groundModule;
    }

    public static class GroundModule extends ElectricalCableModule
    {
        public GroundModule(double resistance) {
            super(resistance);
            terminal = new Circuit.Pin[]{null};
        }
    }
}
