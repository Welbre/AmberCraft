package welbre.ambercraft.module.heat;

import net.minecraft.world.level.block.entity.BlockEntity;
import welbre.ambercraft.Main;
import welbre.ambercraft.blockentity.HeatFurnaceBE;
import welbre.ambercraft.module.ModuleFactory;
import welbre.ambercraft.module.ModuleType;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public final class HeatModuleFactory extends ModuleFactory<HeatModule> {
    public <K extends BlockEntity> HeatModuleFactory(Class<K> BEClass, Consumer<HeatModule> init, Consumer<HeatModule> destroyer, BiConsumer<K, HeatModule> setter, Function<K, HeatModule> getter) {
        super(BEClass, init, destroyer, setter, getter);
    }

    @Override
    public ModuleType<HeatModule> getType() {
        return Main.Modules.HEAT_MODULE_TYPE.get();
    }
}
