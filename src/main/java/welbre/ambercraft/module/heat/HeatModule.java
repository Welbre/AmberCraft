package welbre.ambercraft.module.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.debug.Connection;
import welbre.ambercraft.debug.ScreenNode;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModuleFactory;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.sim.Node;
import welbre.ambercraft.sim.heat.HeatNode;

import java.io.Serializable;
import java.util.*;

public class HeatModule implements Module, Serializable {
    HeatNode node;
    HeatModule father;
    boolean isMaster = true;
    HeatModule[] children;

    boolean isFresh = false;
    public int ID = new Random().nextInt(0,0xffffff);

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
        }
        tag.putInt("ID", ID);
    }

    @Override
    public void readData(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("node")){
            HeatNode n = (HeatNode) Node.fromStringClass(tag.getCompound("node").getString("class"));
            n.fromTag(tag.getCompound("node"));
            node = n;
        }
        ID = tag.getInt("ID");
    }

    /// Returns a copy of children
    public HeatModule[] getChildren() {
        return Arrays.copyOf(this.children,this.children.length);
    }

    private void setRoot()
    {
        List<HeatModule> path = getRootPath();

        for (int i = path.size() - 1; i >= 1; i--)
        {
            HeatModule current = path.get(i);
            HeatModule previous = path.get(i - 1);

            current.removeChild(previous);
            previous.addChild(current);
            current.father = previous;
        }

        path.getLast().isMaster = false;
        isMaster = true;
        father = null;
    }

    /// Disconnect this node from all connections.
    private void disconnectAll(){
        if (father != null)
            father.removeChild(this);

        for (HeatModule child : this.children)
        {
            child.father = null;
            child.setRoot();
        }
    }

    public void connect(HeatModule target)
    {
        List<HeatModule> this_path = this.getRootPath();
        List<HeatModule> target_path = target.getRootPath();

        if (this.father == target || target.father == this)//trying to connect 2 nodes already connected.
            return;
        for (var child : this.children)
            if (child == target)
                return;
        for (var child : target.children)
            if (child == this)
                return;

        if (this_path.getLast() == target_path.getLast())//trying to connect 2 nodes in the same network
        {
            if (this.isMaster)
            {
                target.father = this;
                addChild(target);
            }
            else if (target.isMaster)
            {
                this.father = target;
                target.addChild(this);
            }
            else
            {
                if (this.children.length == 0)//keep as a leaf
                    connect__CRUDE__(target);
                else if (target.children.length == 0)
                    target.connect__CRUDE__(this);
                else if (this_path.size() < target_path.size())
                    target.connect__CRUDE__(this);
                else
                    connect__CRUDE__(target);
            }
            return;
        }

        if (this.isMaster && target.isMaster)
        {
            //Only connect this to the target; therefore, this will no longer be a master.
            connect__CRUDE__(target);
        }
        else if (this.isMaster)
        {
            //Connect 2 nodes, and this is the root, so instead find the root of the target, only connect this to the target.
            //Therefore, all connections will be maintained without the expensive cost of find the root.
            connect__CRUDE__(target);
        }
        else if (target.isMaster)
        {
            //Exact the same solution, but now the available root is the target.
            target.connect__CRUDE__(this);
        }
        else
        {
            //The worst case, no root available, so need to re-Root one a network and after that connect.
            if (this_path.size() > target_path.size())//use the smaller network.
            {
                target.setRoot();
                target.connect__CRUDE__(this);
            }
            else
            {
                this.setRoot();
                this.connect__CRUDE__(target);
            }
        }
    }
    
    /// connect this to the target module.<br> So the target will be the father of this.
    private void connect__CRUDE__(HeatModule target)
    {
        this.isMaster = false;
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

    public List<HeatModule> getRootPath()
    {
        List<HeatModule> path = new ArrayList<>();
        {
            HeatModule current = this;
            while (current != null)
            {
                if (path.size() > 50000)
                    throw new IllegalStateException("Circular dependency detected!");
                path.add(current);
                current = current.father;
            }
        }

        return path;
    }

    public HeatModule getRoot()
    {
        int count = 0;
        HeatModule oldestFather = this;
        var temp = this.father;

        while (temp != null){
            oldestFather = temp;
            temp = temp.father;

            if (count++ > 50000)
                throw new IllegalStateException("Circular dependency detected!");
        }

        return oldestFather.isMaster ? oldestFather : null;
    }

    private boolean shouldBeMaster()
    {
        return father == null;
    }

    public RuntimeException checkInconsistencies() {
        if (isMaster && father != null)
            return new IllegalStateException("corrupted module! is master but isn't root");
        if (!isMaster && father == null)
            return new IllegalStateException("corrupted module! is root but isn't master");

        if (father != null)
        {
            boolean isOk = false;
            for (HeatModule child : father.children)
            {
                if (child == this)
                {
                    isOk = true;
                    break;
                }
            }
            if (!isOk)
                return new RuntimeException("The father(@%x) don't contains this child(@%x)!".formatted(father.ID, ID));

            for (var child : this.children)
                if (child == father)
                    return new RuntimeException("module (@%x) and (@%x) are in a children circular dependency!".formatted(ID, father.ID));
        }

        return null;
    }

    @Override
    public String toString() {
        return "HeatModule{ID = @%x}".formatted(ID);
    }

    @Override
    public void tick(BlockEntity entity){
        if (!this.isMaster)
            return;

        Set<HeatModule> visited = new HashSet<>();
        Queue<HeatModule> queue = new ArrayDeque<>(Collections.singleton(this));
        visited.add(this);

        int count = 0;
        while (!queue.isEmpty())
        {
            HeatModule module = queue.poll();
            if (module.node == null)
                continue;
            HeatNode[] nodes = Arrays.stream(module.children).map(HeatModule::getHeatNode).toArray(HeatNode[]::new);
            module.node.run(nodes);
            for (HeatModule child : module.children)
            {
                if (!visited.contains(child))
                {
                    visited.add(child);
                    queue.add(child);
                }
            }


            if (count++ > 50000)
            {
                AmberCraft.LOGGER.warn("Master %s @%x disabled, by circular dependency while ticking %s @%x!".formatted(
                        this.getClass().getSimpleName(), this.ID,
                        module.getClass().getSimpleName(), module.ID
                ));
                StringBuilder builder = new StringBuilder();
                HashSet<HeatModule> _visited = new HashSet<>();
                _visited.add(module);

                for (HeatModule a : queue)
                {
                    builder.append(a.toString()).append("\n");
                    if (_visited.contains(a))
                        break;
                    _visited.add(a);
                }
                AmberCraft.LOGGER.warn("Queue Status:\n current -> " + module + "\n" + builder + "##redundancy##");

                AmberCraft.LOGGER.error("Circular dependency detected while ticking!", new IllegalStateException("Circular dependency detected while ticking!"));
                this.isMaster = false;
                break;
            }
        }
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
    public <T extends BlockEntity & ModulesHolder> void init(T entity, ModuleFactory<HeatModule,T> factory, LevelAccessor level, BlockPos pos) {
        refresh(entity);
        this.node.setTemperature(HeatNode.GET_AMBIENT_TEMPERATURE(level, pos));

        isMaster = shouldBeMaster();
        }

    /**
     * Rebuild the reference for this module.
     */
    public void refresh(BlockEntity entity) {
        if (isFresh)
            return;

        var level = entity.getLevel();
        if (level == null)
            throw new IllegalStateException("Trying to refresh module while the game isn't loaded!");
        if (level.isClientSide())
            return;

        var pos = entity.getBlockPos();

        for (Direction dir : Direction.values())
            if (level.getBlockEntity(pos.relative(dir)) instanceof ModulesHolder modular)
                for (HeatModule heatModule : modular.getModule(HeatModule.class, dir.getOpposite()))
                        this.connect(heatModule);

        if (isMaster && father != null)
            throw new IllegalStateException("corrupted module!");
        if (!isMaster && father == null)
            throw new IllegalStateException("corrupted module!");

        isFresh = true;
    }

    /**
     * Frees the pointer associated with this module.<br>
     * Before call this, the internal pointer will be null.
     */
    public void free() {
        node = null;
        disconnectAll();
    }
}
