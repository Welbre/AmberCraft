package welbre.ambercraft.blockentity;

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
    public ElectricalModule module = new ElectricalModule(null);

    public ElectricalBE(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public ElectricalBE(BlockPos pos, BlockState state)
    {
        this(AmberCraft.BlockEntity.ELECTRICAL_BE.get(), pos, state);
    }

    @Override
    public @NotNull Module[] getModules() {
        return new Module[]{module};
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

    public ElectricalModule getModule() {
        return module;
    }

    public void setModule(ElectricalModule module) {
        this.module = module;
    }
}
