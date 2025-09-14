package welbre.ambercraft.blockentity.electrical;

import kuse.welbre.sim.electrical.abstractt.Element;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.electrical.ElectricalElementModule;

public class ElectricalBE extends ModulesHolder {
    public ElectricalElementModule electricalModule = new ElectricalElementModule();

    public ElectricalBE(BlockEntityType<?> type, BlockPos pos, BlockState blockState)
    {
        super(type, pos, blockState);
    }

    public ElectricalBE(BlockPos pos, BlockState state)
    {
        this(AmberCraft.BlockEntity.ELECTRICAL_BE.get(), pos, state);
    }

    public Element getElement()
    {
        if (this.electricalModule != null)
            return electricalModule.getElement();
        return null;
    }

    @Override
    public @NotNull Module[] getModules() {
        return new Module[]{electricalModule};
    }

    @Override
    public @NotNull Module[] getModule(Direction direction) {
        return getModules();
    }

    public ElectricalElementModule getElectricalModule() {
        return electricalModule;
    }

    public void setElectricalModule(ElectricalElementModule electricalModule) {
        this.electricalModule = electricalModule;
    }
}
