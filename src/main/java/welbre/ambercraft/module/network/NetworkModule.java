package welbre.ambercraft.module.network;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import welbre.ambercraft.module.Module;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * NetworkModule is a {@link Module} that can automatically connect and disconnect from others NetworkModules at the side.<br>
 * A <a href="https://en.wikipedia.org/wiki/Tree_(abstract_data_type)">tree</a> is used to storage and manage the connections, the NetworkModule it-self is the <a href="https://en.wikipedia.org/wiki/Node_(computer_science)">nodes</a>.<br>
 * Uses a <a href="https://en.wikipedia.org/wiki/Master%E2%80%93slave_(technology)">master/slave</a> to control how the networks work, only the master should tick and control others NetworkModules.<br>
 * <i>Notice that the master is always the root of the network, and the root don't have a father.</i><br>
 * The {@link NetworkModule#isMaster()} method can be used to check if the current Module is the master,
 * and {@link NetworkModule#createMaster()} should be overwritten to return yous own instance of {@link Master}.<br>
 *
 * The {@link NetworkModule#master} is an important part of this module, they control all the behavior of the network and their modules,
 * only one instance is allowed in the network, therefore, only the master must have this field assigned.<br>
 * The master has a compilation system, so after the network change, you must re-compile the master using {@link Master#dirt()}.
 * The {@link NetworkModule#dirtMaster()} can be used to dirt the master from any node in the network.<br>
 * @see Master
 */
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

    /// Dirt the {@link Master} from any module in the network.
    public void dirtMaster(){
        getRoot().master.dirt();
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

    /**
     * Disconnect this module from the network.<br>
     * This method is very friendly. If needed, this splits the network, re-assigns the master, and dirts all masters in the process.
     */
    public void disconnectAll(){
        if (father != null)
        {
            father.removeChild(this);
            NetworkModule root = father.getRoot();
            if (root == null)
                father.setRoot();
            else
                root.master.dirt();
        }

        for (NetworkModule child : this.children)
        {
            child.father = null;
            NetworkModule root = child.getRoot();
            if (root == null)
                child.setRoot();
            else
                root.master.dirt();
        }
    }

    /**
     * Connects this module to <code>target</code>.<br>
     * Is important to understand the behavior of this method, based on the {@code this} network state, and {@code target} network state, this method can do different things.<br>
     * First this method checks if the {@code this} and {@code target} is already connected. If it is true, the method only returns.<br><br>
     *
     * Now the method checks if the 2 modules to be connected is on the same network, this is an important part because the connections can occur between modules in the same network, and this must be handled differently.<br>
     * If the modules is in the same network, first is verified if {@code this} or {@code target} is a master,
     * <i><u>a master is always easier to connect, because they can be a child without re-structure the network</i></u>.
     * If this is the master, set the target as its child. If the target is the master, do the opposite. If no one is the master, a special case tries to conserve the leaf modules to avoid circular dependence.<br><br>
     *
     * In the case that 2 different connections are happening, a similar code runs, looking for the master and performing an easier connection.
     * Only if no master is founded, {@link NetworkModule#setRoot()} is invoked in the shortest network, making it the master and reducing to the last case.<br>
     * @param target the module that this will connect to.
     */
    public void connect(NetworkModule target)
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

    /// @return a list with the path to the root.<br>
    /// Notice that the last element is the root, and the first is this.
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

    /// @return the root/master of the network.
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

    /// used only in diagnostic, test, and debug.
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

    /// The master factory, you must create you own {@link Master} and instantiate where.
    public abstract Master createMaster();
}
