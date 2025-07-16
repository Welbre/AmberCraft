package welbre.ambercraft.module.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModuleFactory;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.sim.heat.HeatNode;
import welbre.ambercraft.sim.network.Network;
import welbre.ambercraft.sim.network.Node;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Queue;

public class HeatModule implements Module {
    HeatNode node;
    HeatModule father;
    boolean isMaster = false;
    HeatModule[] children;

    public HeatModule() {
        children = new HeatModule[0];
    }

    public HeatNode getHeatNode(){
        return node;
    }
    
    @Override
    public void writeData(CompoundTag tag, HolderLookup.Provider registries) {
        if (node != null)
        {
            tag.put("node", node.toTag());
            tag.putBoolean("isMaster", isMaster);
        }
    }

    @Override
    public void readData(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("node")){
            HeatNode n = (HeatNode) Node.fromStringClass(tag.getCompound("node").getString("class"));
            n.fromTag(tag.getCompound("node"));
            node = n;
            isMaster = tag.getBoolean("isMaster");
        }
    }

    /**
     * Allocates a new instance of a {@link HeatNode} and registers it within the network,
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
    public <T extends BlockEntity & ModulesHolder> void init(T entity, ModuleFactory<HeatModule,T> factory, LevelAccessor level, BlockPos pos) {
        for (Direction dir : Direction.values())
            if (level.getBlockEntity(pos.relative(dir)) instanceof ModulesHolder modular)
                for (HeatModule heatModule : modular.getModule(HeatModule.class, dir.getOpposite()))
                    this.connect(heatModule);
        
        this.node.setTemperature(HeatNode.GET_AMBIENT_TEMPERATURE(level, pos));

        isMaster = shouldBeMaster();
    }

    /**
     * Frees the pointer associated with this module.<br>
     * Before call this, the internal pointer will be null.
     */
    public void free() {
        node = null;
        disconnectAll();
    }

    public void disconnectFather(){
        if (father != null)
            father.removeChild(this);
        father = null;
    }

    /// Disconnect this node from all connections.
    private void disconnectAll(){
        //check if the father network has a master, if not, set the father as the master of the network;
        if (father != null)
        {
            father.removeChild(this);
            HeatModule master = father.getMaster();
            if (master == null)
                father.isMaster = true;
        }
        this.father = null;

        for (HeatModule child : children)
            child.father = null;
        for (HeatModule child : children)
            //check if the children have a master, if not, set the child as master of hin network.
            if (child.getMaster() == null)
                child.isMaster = true;
        children = new HeatModule[0];
    }
    
    /// connect this in the target module.
    public void connect(HeatModule target)
    {
        disconnectFather();
        //check if the network has 2 masters, if true, then remove the target mester.
        {
            HeatModule this_master = this.getMaster();
            HeatModule target_master = target.getMaster();
            if (this_master != null && target_master != null)
                target_master.isMaster = false;
        }
        this.father = target;
        target.addChild(this);
    }
    
    /// Copy's the children array and the child on it.<br> don't use it to connect 2 modules! 
    private void addChild(HeatModule child){
        HeatModule[] newChildren = new HeatModule[children.length + 1];
        System.arraycopy(children, 0, newChildren, 0, children.length);
        newChildren[children.length] = child;
        children = newChildren;
    }

    ///remove the children from the array.<br> don't use it to disconnect 2 modules!
    private void removeChild(HeatModule child)
    {
        if (children.length == 0)
            return;
        HeatModule[] newChildren = new HeatModule[children.length - 1];

        int index = 0;
        for (HeatModule module : children)
            if (module != child)
                newChildren[index++] = module;

        children = newChildren;
    }

    /// Get the master of the network
    public HeatModule getMaster()
    {
        if (isMaster)
            return this;
        if (father != null && father.isMaster)
            return father;

        for (HeatModule child : children)
        {
            HeatModule master = child.getMaster();
            if (master != null)
                return master;
        }
        return null;
    }

    private boolean shouldBeMaster()
    {
        return father == null && children.length == 0;
    }

    @Override
    public void tick(){
        if (!this.isMaster)
            return;

        HeatModule oldestFather = this;
        //get the oldest father.
        {
            var temp = this.father;

            while (temp != null){
                oldestFather = temp;
                temp = temp.father;
            }
        }

        Queue<HeatModule> queue = new ArrayDeque<>(Collections.singleton(oldestFather));
        while (!queue.isEmpty())
        {
            HeatModule module = queue.poll();
            //todo terrible code. refactor this later.
            if (module.node != null)
                module.node.run(Arrays.stream(module.children).map(a -> a.node).toArray(HeatNode[]::new));
            queue.addAll(Arrays.asList(module.children));
        }
    }
}
