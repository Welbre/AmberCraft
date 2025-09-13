package welbre.ambercraft.module.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LevelAccessor;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.DebugToolInfo;
import welbre.ambercraft.module.ModuleType;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.network.Master;
import welbre.ambercraft.module.network.NetworkModule;
import welbre.ambercraft.sim.Node;
import welbre.ambercraft.sim.heat.HeatNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HeatModule extends NetworkModule implements Serializable, DebugToolInfo {
    HeatNode node;

    public HeatModule() {

    }

    public HeatNode getHeatNode(){
        return node;
    }
    
    @Override
    public void writeData(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeData(tag, registries);
        if (node != null)
            tag.put("node", node.toTag());
    }

    @Override
    public void readData(CompoundTag tag, HolderLookup.Provider registries) {
        super.readData(tag, registries);
        if (tag.contains("node")){
            HeatNode n = (HeatNode) Node.fromStringClass(tag.getCompound("node").getString("class"));
            n.fromTag(tag.getCompound("node"));
            node = n;
        }
    }

    public String getMultimeterString(){
        return "%.3fºC".formatted(node.getTemperature());
    }

    @Override
    public String toString() {
        return "HeatModule{ID = @%x}".formatted(ID);
    }

    @Override
    public void tick(ModulesHolder entity){
        if (!this.isMaster())
            return;
        Profiler.get().push("HeatModuleTick");

        master.tick(entity);

        Profiler.get().pop();
    }

    /**
     * Allocates father new instance of father {@link HeatNode} and registers it within the network,
     * @return the newly created and registered {@link HeatNode} instance.
     */
    public HeatNode alloc() {
        node = new HeatNode();
        return node;
    }

    /**
     * Initialize the current module in the world, using the blockEntity, the level, and the position.<br>
     *
     * Server side only!
     */
    public <T extends ModulesHolder> void init(T entity, LevelAccessor level, BlockPos pos)
    {
        refresh(entity);
        this.node.setTemperature(HeatNode.GET_AMBIENT_TEMPERATURE(level, pos));

        if (isRoot())
            master = createMaster();
    }

    /**
     * Frees the pointer associated with this module.<br>
     * Before call this, the internal pointer will be null.
     */
    public void free() {
        node = null;
        disconnectAll();
    }

    @Override
    public Master createMaster() {
        return new HeatModuleMaster(this);
    }

    @Override
    public ModuleType<?> getType() {
        return AmberCraft.ModuleTypes.HEAT_MODULE_TYPE.get();
    }

    @Override
    public List<Component> getInfo() {
        ArrayList<Component> list = new ArrayList<>();
        if (node != null)
        {
            list.add(Component.literal("Temperature: %.2f ºC".formatted(node.getTemperature())));
            list.add(Component.literal("Conductivity: %.2f W/ºC".formatted(node.getThermalConductivity())));
            list.add(Component.literal("Capacidade: %.2f J/ºC".formatted(node.getThermalMass())));
            list.add(Component.literal("Ambient temperature: %.2f ºC".formatted(node.getEnvTemperature())));
            list.add(Component.literal("Ambient conductivity: %.2f W/ºC".formatted(node.getEnvConductivity())));
        } else
            list.add(Component.literal("Heat not is null!").withColor(DyeColor.RED.getTextColor()));
        return list;
    }
}
