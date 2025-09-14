package welbre.ambercraft.blocks.electrical;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.electrical.ElectricalBE;
import welbre.ambercraft.blocks.FreeRotationBlock;
import welbre.ambercraft.module.Module;

public class VoltageSourceBE extends ElectricalBE {
    public VoltageSourceBE(BlockPos pos, BlockState blockState) {
        super(AmberCraft.BlockEntity.VOLTAGE_SOURCE_BE.get(), pos, blockState);
    }

    @Override
    public @NotNull welbre.ambercraft.module.Module[] getModule(Direction direction) {
        assert getLevel() != null;

        var facing = this.getLevel().getBlockState(this.getBlockPos()).getValue(FreeRotationBlock.FACING);
        if (facing == direction)
            return new welbre.ambercraft.module.Module[]{this.getElectricalModule().getTerminalA()};
        else if (facing == direction.getOpposite())
            return new welbre.ambercraft.module.Module[]{this.getElectricalModule().getTerminalB()};

        return new Module[0];
    }
}
