package welbre.ambercraft.module.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModuleFactory;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.heat.HeatModule;
import welbre.ambercraft.sim.Node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public abstract class NetworkModule implements Module, Serializable {
    protected NetworkModule father = null;
    protected Master master = createMaster();
    protected NetworkModule[] children = new NetworkModule[0];

    protected boolean isFresh = false;
    public int ID = new Random().nextInt(0,0xffffff);

    public NetworkModule() {
    }

    @Override
    public void writeData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("ID", ID);
    }

    @Override
    public void readData(CompoundTag tag, HolderLookup.Provider registries) {
        ID = tag.getInt("ID");
    }

    /// Returns a copy of children
    public NetworkModule[] getChildren() {
        return Arrays.copyOf(this.children,this.children.length);
    }

    public void dirtMaster(){
        getRoot().master.isCompiled = false;
    }

    private void setRoot()
    {
        List<NetworkModule> path = getRootPath();

        for (int i = path.size() - 1; i >= 1; i--)
        {
            NetworkModule current = path.get(i);
            NetworkModule previous = path.get(i - 1);

            current.removeChild(previous);
            previous.addChild(current);
            current.father = previous;
        }

        path.getLast().master = null;
        master = createMaster();
        father = null;
    }

    /// Disconnect this node from all connections.
    protected void disconnectAll(){
        if (father != null)
        {
            father.removeChild(this);
            NetworkModule root = father.getRoot();
            if (root == null)
                father.setRoot();
            else
                root.master.isCompiled = false;
        }

        for (NetworkModule child : this.children)
        {
            child.father = null;
            NetworkModule root = child.getRoot();
            if (root == null)
                child.setRoot();
            else
                root.master.isCompiled = false;
        }
    }

    protected void connect(NetworkModule target)
    {
        List<NetworkModule> this_path = this.getRootPath();
        List<NetworkModule> target_path = target.getRootPath();

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
            if (this.isMaster())
            {
                target.father = this;
                addChild(target);
            }
            else if (target.isMaster())
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
        }
        else
        {
            if (this.isMaster() && target.isMaster())
            {
                //Only connect this to the target; therefore, this will no longer be a master.
                connect__CRUDE__(target);
            } else if (this.isMaster())
            {
                //Connect 2 nodes, and this is the root, so instead find the root of the target, only connect this to the target.
                //Therefore, all connections will be maintained without the expensive cost of finding the root.
                connect__CRUDE__(target);
            } else if (target.isMaster())
            {
                //Exact the same solution, but now the available root is the target.
                target.connect__CRUDE__(this);
            } else
            {
                //The worst case, no root available, so need to re-Root one a network and after that connect.
                if (this_path.size() > target_path.size())//use the smaller network.
                {
                    target.setRoot();
                    target.connect__CRUDE__(this);
                } else
                {
                    this.setRoot();
                    this.connect__CRUDE__(target);
                }
            }
        }
        dirtMaster();
    }

    public boolean isMaster(){
        return master != null;
    }

    /// connect this to the target module.<br> So the target will be the father of this.
    private void connect__CRUDE__(NetworkModule target)
    {
        this.master = null;
        this.father = target;
        target.addChild(this);
    }

    /// Copy's the children array and the child on it.<br> don't use it to connect 2 modules!
    private void addChild(NetworkModule child){
        NetworkModule[] newChildren = new NetworkModule[children.length + 1];
        System.arraycopy(children, 0, newChildren, 0, children.length);
        newChildren[children.length] = child;
        children = newChildren;
    }

    ///remove the children from the array.<br> don't use it to disconnect 2 modules!
    private void removeChild(NetworkModule child)
    {
        if (children.length == 0)
            return;
        NetworkModule[] newChildren = new NetworkModule[children.length - 1];

        int index = 0;
        for (NetworkModule module : children)
            if (module != child)
                newChildren[index++] = module;

        children = newChildren;
    }

    public List<NetworkModule> getRootPath()
    {
        List<NetworkModule> path = new ArrayList<>();
        {
            NetworkModule current = this;
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

    public NetworkModule getRoot()
    {
        int count = 0;
        NetworkModule oldestFather = this;
        var temp = this.father;

        while (temp != null){
            oldestFather = temp;
            temp = temp.father;

            if (count++ > 50000)
                throw new IllegalStateException("Circular dependency detected!");
        }

        return oldestFather.isMaster() ? oldestFather : null;
    }

    protected boolean shouldBeMaster()
    {
        return father == null;
    }

    public RuntimeException checkInconsistencies() {
        if (isMaster() && father != null)
            return new IllegalStateException("corrupted module! is master but isn't root");
        if (!isMaster() && father == null)
            return new IllegalStateException("corrupted module! is root but isn't master");

        if (father != null)
        {
            boolean isOk = false;
            for (NetworkModule child : father.children)
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

    public NetworkModule getFather() {
        return father;
    }

    public Master getMaster() {
        return master;
    }

    public abstract Master createMaster();
    public abstract Node alloc();
    public abstract void free();
    public abstract <T extends BlockEntity & ModulesHolder> void init( T entity, ModuleFactory<HeatModule,T> factory, LevelAccessor level, BlockPos pos);
}
