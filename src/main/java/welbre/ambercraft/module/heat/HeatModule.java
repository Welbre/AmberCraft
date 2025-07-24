package welbre.ambercraft.module.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import welbre.ambercraft.AmberCraft;
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

    private void setRoot()
    {
        List<HeatModule> path = new ArrayList<>();
        {
            HeatModule current = this;
            while (current != null)
            {
                if (path.size() > 5000)
                    return;
                path.add(current);
                current = current.father;
            }
        }

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
        //todo only to debug, remove it later
        if (isMaster && father != null)
            throw new IllegalStateException("corrupted module!");
        if (!isMaster && father == null)
            throw new IllegalStateException("corrupted module!");

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
        HeatModule this_root = this.getRoot();
        HeatModule target_root = target.getRoot();
        if (this_root == target_root)//check if is already connected!
            return;

        if (this.isMaster && target.isMaster)//Simplest case, only add this to the target children.
        {
            //todo check which is easier to re-root using some future root info
            connect__CRUDE__(target);
        }
        else if (this.isMaster)//if this is master, and the target isn't a master, re-root the target and connect the target to this.
        {
            target.setRoot();
            target.connect__CRUDE__(this);
        }
        else if (target.isMaster)//if the target is master and this isn't, re-root this and connect this to the target.
        {
            setRoot();
            connect__CRUDE__(target);
        }
        else//no master connection, so re-root both and connect this to target.
        {
            setRoot();
            target.setRoot();
            connect__CRUDE__(target);
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

    public HeatModule getRoot()
    {
        HeatModule oldestFather = this;
        var temp = this.father;

        while (temp != null){
            oldestFather = temp;
            temp = temp.father;
        }

        return oldestFather.isMaster ? oldestFather : null;
    }

    private boolean shouldBeMaster()
    {
        return father == null;
    }

    @Override
    public void tick(BlockEntity entity){
        if (!this.isMaster)
            return;

        var oldestFather = getRoot();

        Queue<HeatModule> queue = new ArrayDeque<>(Collections.singleton(oldestFather));
        int count = 0;
        while (!queue.isEmpty())
        {
            HeatModule module = queue.poll();
            //todo terrible code. refactor this later.
            if (module.node != null)
            {
                module.node.run(Arrays.stream(module.children).map(a -> a.node).toArray(HeatNode[]::new));
                entity.setChanged();
            }
            queue.addAll(Arrays.asList(module.children));
            if (count++ > 1000)
            {
                AmberCraft.LOGGER.warn("Master %s @%x disabled, by circular dependency in %s @%x!".formatted(
                        module.getClass().getSimpleName(), module.ID,
                        this.getClass().getSimpleName(), this.ID
                ));
                AmberCraft.LOGGER.error("Circular dependency detected!", new IllegalStateException("Cyclic dependency detected!"));
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
        if (level.isClientSide)
            return;

        var pos = entity.getBlockPos();

        for (Direction dir : Direction.values())
            if (level.getBlockEntity(pos.relative(dir)) instanceof ModulesHolder modular)
                for (HeatModule heatModule : modular.getModule(HeatModule.class, dir.getOpposite()))
                    this.connect(heatModule);

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
