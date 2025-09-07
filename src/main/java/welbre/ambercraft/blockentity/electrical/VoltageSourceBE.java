package welbre.ambercraft.blockentity.electrical;

import kuse.welbre.sim.electrical.elements.VoltageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.ElectricalBE;
import welbre.ambercraft.blocks.electrical.VoltageSourceBlock;
import welbre.ambercraft.module.Module;

public class VoltageSourceBE extends ElectricalBE {
    public VoltageSourceBE(BlockPos pos, BlockState state) {
        super(AmberCraft.BlockEntity.VOLTAGE_SOURCE_BE.get(), pos, state);
        var vs = new VoltageSource();
        vs.connectB(null);//FIXIT at the moment the MNA is breaking because don't find a ground, remove this after fix the MNA
        setElement(vs);
    }

    public VoltageSource getElement()
    {
        if (this.elementPointer[0] instanceof VoltageSource source)
            return source;
        else
            throw new IllegalStateException("Voltage source with a wrong element in the electrical module!");
    }

    @Override
    public @NotNull Module[] getModule(Direction dir) {
        Direction facing = getBlockState().getValue(VoltageSourceBlock.FACING);
        if (facing == dir)
            return new Module[]{this.getPinA()};
        else if (facing.getOpposite() == dir)
            return new Module[]{this.getPinB()};
        else
            return new Module[0];
    }
}
