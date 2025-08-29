package welbre.ambercraft.module.electrical;

import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.ModuleType;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.network.Master;
import welbre.ambercraft.module.network.NetworkModule;

public class ElectricalModule extends NetworkModule {
    @Override
    public Master createMaster() {
        return new ElectricalModulesMaster(this);
    }

    @Override
    public void tick(ModulesHolder entity) {

    }


    @Override
    public ModuleType<?> getType() {
        return AmberCraft.ModuleTypes.ELECTRICAL_MODULE_TYPE.get();
    }

    public void alloc() {

    }

    public void free() {

    }
}
