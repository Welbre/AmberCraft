package welbre.ambercraft.blockentity;

import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.elements.VoltageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.electrical.ElectricalModule;

public class ElectricalBE extends ModulesHolder {
    public ElectricalModule electricalModule = new ElectricalModule();

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
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public @NotNull Module[] getModules() {
        return new Module[]{electricalModule};
    }

    @Override
    public @NotNull Module[] getModule(Direction direction) {
        return getModules();
    }

    @Override
    public @NotNull Module[] getModule(Object object) {
        if (object instanceof Direction dir)
            return getModule(dir);
        return new Module[0];
    }

    public ElectricalModule getElectricalModule() {
        return electricalModule;
    }

    public void setElectricalModule(ElectricalModule electricalModule) {
        this.electricalModule = electricalModule;
    }
}
