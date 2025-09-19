package welbre.ambercraft.blockentity.electrical;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.Module;

/**
 * A generic electrical block entity that automatically handles the rotation property {@link BlockStateProperties#FACING} from the block.<br>
 * The north returns the pinA, and in the opposite direction the pinB.<br>
 * All blocks that use this BlockEntity must have a <code>facing</code> property! Or an exception can be trowed.<br>
 * <div color="#FFCC00">Remember to add yous block that uses this BlockEntity in {@link AmberCraft.Blocks#DIRECTIONAl_ELECTRICAL_BE_USERS}</div>
 */
public class DirectionalElectricalBE extends ElectricalBE {
    public DirectionalElectricalBE(BlockPos pos, BlockState state) {
        super(AmberCraft.BlockEntity.DIRECTIONAL_ELECTRICAL_BE.get(), pos, state);
    }

    @Override
    public @NotNull welbre.ambercraft.module.Module[] getModule(Direction direction) {
        assert getLevel() != null;

        var facing = this.getLevel().getBlockState(this.getBlockPos()).getValue(BlockStateProperties.FACING);
        if (facing == direction)
            return new welbre.ambercraft.module.Module[]{this.getElectricalModule().getTerminalA()};
        else if (facing == direction.getOpposite())
            return new welbre.ambercraft.module.Module[]{this.getElectricalModule().getTerminalB()};

        return new Module[0];
    }
}
