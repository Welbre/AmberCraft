package welbre.ambercraft.cables;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import welbre.ambercraft.blockentity.FacedCableBlockEntity;
import welbre.ambercraft.module.Module;

public class FaceBrain {
    private CableType type;
    private Module[] modules;

    public FaceBrain(CableType type, FacedCableBlockEntity cable) {
        this.type = type;
        this.modules = type.createModules(cable);
    }

    public void tick(Level level, BlockPos pos, BlockState state, FacedCableBlockEntity cable)
    {
        for (Module module : modules)
            module.tick(level, pos, state, cable);
    }

    public Module[] getModules() {
        return modules;
    }
}
