package welbre.ambercraft.module.electrical;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.profiling.Profiler;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.DebugToolInfo;
import welbre.ambercraft.module.ModuleType;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.network.Master;
import welbre.ambercraft.module.network.NetworkModule;

import java.util.List;

/**
 * Used only in cable.<br>
 * This module computes the current cables resistence.
 * The {@link ElectricalCableModule#resistence} is the resistence between 2 cables, the resistence value is computed by the average resistence.
 */
public class ElectricalCableModule extends NetworkModule implements DebugToolInfo {
    public double resistence = 0.001; //1mOhm

    public ElectricalCableModule() {
    }

    public ElectricalCableModule(double resistence) {
        this.resistence = resistence;
    }

    @Override
    public Master createMaster() {
        return new ElectricalModulesMaster(this);
    }

    @Override
    public void writeData(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeData(tag, registries);
        tag.putDouble("resistence", resistence);
    }

    @Override
    public void readData(CompoundTag tag, HolderLookup.Provider registries) {
        super.readData(tag, registries);
        resistence = tag.getDouble("resistence");
    }

    @Override
    public void tick(ModulesHolder entity) {
        if (!this.isMaster() || entity.getLevel().isClientSide)
            return;
        Profiler.get().push("HeatModuleTick");

        master.tick(entity);

        Profiler.get().pop();
    }

    @Override
    public ModuleType<?> getType()
    {
        return AmberCraft.ModuleTypes.ELECTRICAL_CABLE_MODULE_TYPE.get();
    }

    @Override
    public List<Component> getInfo() {
        return List.of(Component.literal("Resistence: %.2fΩ".formatted(resistence)));
    }
}
