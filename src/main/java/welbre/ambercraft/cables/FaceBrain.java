package welbre.ambercraft.cables;

import welbre.ambercraft.blockentity.FacedCableBE;
import welbre.ambercraft.module.Module;

public class FaceBrain {
    private CableType type;
    private Module[] modules;

    public FaceBrain(CableType type, FacedCableBE cable) {
        this.type = type;
        this.modules = type.createModules(cable);
    }

    public Module[] getModules() {
        return modules;
    }
}
