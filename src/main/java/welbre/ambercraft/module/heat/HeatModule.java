package welbre.ambercraft.module.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import welbre.ambercraft.module.ModuleFactory;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.network.Master;
import welbre.ambercraft.module.network.NetworkModule;
import welbre.ambercraft.sim.Node;
import welbre.ambercraft.sim.heat.HeatNode;

import java.io.Serializable;
import java.util.Random;

public class HeatModule extends NetworkModule implements Serializable {
    HeatNode node;

    public HeatModule() {
        children = new HeatModule[0];
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
    public void tick(BlockEntity entity){
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
    public <T extends ModulesHolder> void init(T entity, ModuleFactory<HeatModule,T> factory, LevelAccessor level, BlockPos pos)
    {
        refresh(entity);
        this.node.setTemperature(HeatNode.GET_AMBIENT_TEMPERATURE(level, pos));

        if (shouldBeMaster())
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
}
