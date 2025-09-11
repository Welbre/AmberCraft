package welbre.ambercraft.blockentity.electrical;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blocks.FreeRotationBlock;
import welbre.ambercraft.module.Module;

public class VoltageSourceBE extends ElectricalBE {
    public VoltageSourceBE(BlockPos pos, BlockState state) {
        super(AmberCraft.BlockEntity.VOLTAGE_SOURCE_BE.get(), pos, state);
    }

    @Override
    public @NotNull Module[] getModule(Direction direction) {
        assert getLevel() != null;

        var facing = this.getLevel().getBlockState(this.getBlockPos()).getValue(FreeRotationBlock.FACING);
        if (facing == direction)
            return new Module[]{this.getElectricalModule().getTerminalA()};
        else if (facing == direction.getOpposite())
            return new Module[]{this.getElectricalModule().getTerminalB()};

        return new Module[0];
    }
}
