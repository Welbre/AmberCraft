package welbre.ambercraft.blockentity.electrical;

import kuse.welbre.sim.electrical.elements.VoltageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.ElectricalBE;
import welbre.ambercraft.module.electrical.ElectricalModule;

public class VoltageSourceBE extends ElectricalBE {
    public VoltageSourceBE(BlockPos pos, BlockState state) {
        super(AmberCraft.BlockEntity.VOLTAGE_SOURCE_BE.get(), pos, state);
        this.module = new ElectricalModule(new VoltageSource());
    }

    public VoltageSource getElement()
    {
        if (this.module.element instanceof VoltageSource source)
            return source;
        else
            throw new IllegalStateException("Voltage source with a wrong element in the electrical module!");
    }
}
