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

/**
 * A generic electrical BlockEntity.<br>
 * This class is a helper, has method to help with {@link welbre.ambercraft.module.ModuleFactory},
 * stores an empty ElectricalElementModule that can easily access from parent class.<br>
 * <div color="#FFCC00">Remember to add yous block that uses this BlockEntity in {@link AmberCraft.Blocks#ELECTRICAL_BE_USERS}</div>
 */
public class ElectricalBE extends ModulesHolder {
    protected ElectricalElementModule electricalModule = new ElectricalElementModule();

    public ElectricalBE(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public ElectricalBE(BlockPos pos, BlockState state) {
        this(AmberCraft.BlockEntity.ELECTRICAL_BE.get(), pos, state);
    }

    public Element getElement() {
        if (this.electricalModule != null)
            return electricalModule.getElement();
        return null;
    }

    @Override
    public @NotNull Module[] getModules() {
        return new Module[]{electricalModule, electricalModule.getTerminalA(), electricalModule.getTerminalB()};
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
