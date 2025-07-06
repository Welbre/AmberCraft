package welbre.ambercraft.module.heat;

import welbre.ambercraft.Main;
import welbre.ambercraft.module.ModuleFactory;
import welbre.ambercraft.module.ModuleType;

import java.util.function.Consumer;

public final class HeatModuleFactory extends ModuleFactory<HeatModule> {
    public HeatModuleFactory(Consumer<HeatModule> setter) {
        super(setter);
    }

    @Override
    public ModuleType<HeatModule> getType() {
        return Main.Modules.HEAT_MODULE_TYPE.get();
    }
}
