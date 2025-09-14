package welbre.ambercraft.module.network;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModulesHolder;

import java.io.Serializable;
import java.util.*;

/**
 * NetworkModule is a {@link Module} that can automatically connect and disconnect from others NetworkModules at the side.<br>
 * A <a href="https://en.wikipedia.org/wiki/Graph_(discrete_mathematics)">graph</a> is used to storage and manage the connections, the NetworkModule it-self is the <a href="https://en.wikipedia.org/wiki/Node_(computer_science)">nodes</a>.<br>
 * Uses a <a href="https://en.wikipedia.org/wiki/Master%E2%80%93slave_(technology)">master/slave</a> to control how the networks work, only the master should tick and control others NetworkModules.<br>
 *
 * The {@link NetworkModule#master} is an important part of this module, they control all the behavior of the network and their modules,
 * only one instance is allowed in the network, therefore, only the master must have this field assigned.<br>
 * The master has a compilation system, so after the network change, you must re-compile the master using {@link Master#dirt()}.
 * The {@link NetworkModule#dirtMaster()} can be used to dirt the master from any node in the network.<br>
 *
 * The {@link NetworkModule#isMaster()} method can be used to check if the current Module is the master,
 * and {@link NetworkModule#createMaster()} should be overwritten to return yous own instance of {@link Master}.<br>
 * The root is a filed that signatures the network, modules in the same network must agree about the root module, therefore, only one root exits in the network.
 * Because this behavior only the root can be the master, the {@link #isRoot()} can be used to check if the current module is the root.<br>
 *
 * If you are extending the class, you should check if {@link #isMaster()} and {@link #isRoot()}, if isn't true, the module is corrupted!
 * This check isn't made, but {@link #checkInconsistencies()} can be used to do that and other integrity checks.
 *
 * @see Master
 */
public abstract class NetworkModule implements Module, Serializable, Iterable<NetworkModule> {
    /**
     * Points to the network master.<br>
     * Only one root is allowed in the network;
     * so, the root can be used to check if 2 nodes is in the same network.
     */
    protected @NotNull NetworkModule root = this;
    protected Master master = createMaster();
    protected NetworkModule[] neighbors = new NetworkModule[0];

    protected boolean isFresh = false;
    public int ID = new Random().nextInt(0,0xffffff);

    public NetworkModule() {

    }

    private void setRoot()
    {
        if (root == this)//is already the root
            return;

        this.root.master = null;
        for (NetworkModule module : this)
            module.root = this;

        this.master = createMaster();
    }

    /**
     * Disconnect this module from the network.<br>
     * This method is very friendly. If needed, this splits the network, re-assigns the master, and dirt all masters in the process.
     */
    public void disconnectAll(){
        if (neighbors.length > 0)//no connection, no remotion.
        {
            for (NetworkModule neighbor : neighbors)
                neighbor.removeNeighbor(this);

            for (NetworkModule neighbor : neighbors)
            {
                if (neighbor.isRoot())
                {
                    neighbor.dirtMaster();
                    continue;
                }
                List<NetworkModule> path = neighbor.getPath(this.root);
                if (path.isEmpty())//if after remotion, it is isolated, set as root, a split happens;
                {
                    for (NetworkModule module : neighbor)
                        module.root = neighbor;

                    neighbor.master = neighbor.createMaster();
                }
                else
                    neighbor.dirtMaster();
            }
        }

        this.neighbors = new NetworkModule[0];
        this.root = this;
        this.master = createMaster();
    }

    /**
     * Connects <code>this</code> module to <code>target</code>.<br>
     * This method is very friendly. If needed, they merge the network, re-root and dirt all masters in the process.
     * @return if a new connection has been created.
     */
    public boolean connect(NetworkModule target)
    {
        if (this.root == target.root) //always in the same network
        {
            //check the same connection will be done again
            for (NetworkModule neighbor : this.neighbors)
                if (neighbor == target)// this already contains the target.
                    return false;
        } else {
            //at this point 2 different networks are connecting.
            //first, clear up the target network; these have your own master and root,
            //so, set the root to this.root (the root of this network), and clear up any master in the target network
            for (var nm : target)
            {
                nm.root = this.root;
                nm.master = null;
            }
        }

        // only now, add the target to this network.
        addNeighbor(target);
        target.addNeighbor(this);

        dirtMaster();//dirt the master to compile all changes
        return true;
    }

    /// @return If the current module is the master.
    public boolean isMaster()
    {
        return master != null;
    }

    /// Copy the neighborhood array and add the neighbor on it.<br> don't use it to connect 2 modules!
    private void addNeighbor(NetworkModule neighbor){
        NetworkModule[] newChildren = new NetworkModule[neighbors.length + 1];
        System.arraycopy(neighbors, 0, newChildren, 0, neighbors.length);
        newChildren[neighbors.length] = neighbor;
        neighbors = newChildren;
    }

    ///remove the children from the array.<br> don't use it to disconnect 2 modules!
    private void removeNeighbor(NetworkModule neighbor)
    {
        if (neighbors.length == 0)
            return;
        NetworkModule[] neighborhood = new NetworkModule[neighbors.length - 1];

        if (neighborhood.length > 0)
        {
            int index = 0;
            for (NetworkModule module : neighbors)
                if (module != neighbor)
                    neighborhood[index++] = module;
        }

        neighbors = neighborhood;
    }

    /// returns the path to the target; the path is an arraylist that always contains <code>this</code> at the first position and <code>target</code> at last position.<br>
    /// If the size is one, this means that this == target. If the size is zero, the pathfinder can't find a path from <code>this</code> to <code>target</code>.<br>
    /// This method is bidirectional, so you can invert this and target, and it will return the same path but inverted.
    public List<NetworkModule> getPath(NetworkModule target)
    {
        Set<NetworkModule> visited = new HashSet<>();
        List<NetworkModule> path = new ArrayList<>();
        GET_PATH(this, target, visited, path);

        return path;
    }

    private static boolean GET_PATH(NetworkModule where, NetworkModule target, Set<NetworkModule> visited, List<NetworkModule> path)
    {
        if (visited.contains(where))
            return false;
        visited.add(where);
        path.add(where);

        if (where == target)
            return true;
        else
            for (NetworkModule neighbor : where.neighbors)
                if (GET_PATH(neighbor, target, visited, path)) return true;

        path.removeLast();
        return false;
    }

    /// @return If the current module is the root of the network.
    public boolean isRoot()
    {
        return root == this;
    }

    /// used only in diagnostic, test, and debug.
    /// @return null if success, or an array of execution in the module.
    public RuntimeException[] checkInconsistencies() {
        List<RuntimeException> errors = new ArrayList<>();
        if (isMaster() && !isRoot())
            errors.add(new IllegalStateException("module 0x%x corrupted! is master but isn't root".formatted(this.ID)));
        if (!isMaster() && isRoot())
            errors.add(new IllegalStateException("module 0x%x corrupted! is root but isn't master".formatted(this.ID)));

        for (NetworkModule n : neighbors)
        {
            if (n.root != this.root)//check if the neighbor agrees about the root.
                errors.add(new IllegalStateException("module 0x%x corrupted! with root 0x%X don't agree with module 0x%X and its root 0x%X".formatted(this.ID, this.root.ID, n.ID, n.root.ID)));
            boolean error = true;
            //checks if the neighbor has this as a neighbor
            for (var nn : n.neighbors)
            {
                if (nn == this)
                {
                    error = false;
                    break;
                }
            }
            if (error)
                errors.add(new IllegalStateException("module 0x%x corrupted! neighbor 0x%X doesn't have this as a neighbor".formatted(this.ID, n.ID)));
        }

        if (!errors.isEmpty())
            return errors.toArray(new RuntimeException[0]);
        return null;
    }

    /**
     * Search in the sides looking for {@link ModulesHolder} to connect with.
     * @param entity the holder that this is stored.
     */
    public void refresh(ModulesHolder entity) {
        if (isFresh)
            return;

        var level = entity.getLevel();
        if (level == null)
            throw new IllegalStateException("Trying to refresh module while the game isn't loaded!");
        if (level.isClientSide())
            return;

        var pos = entity.getBlockPos();

        for (Direction dir : Direction.values())//check all faces in the BlockEntity
            if (List.of(entity.getModule(dir)).contains(this))//if dir face contains "this" module
                if (level.getBlockEntity(pos.relative(dir)) instanceof ModulesHolder modular)//check if the block in the face direction is a ModulesHolder
                    for (Module module : modular.getModule(dir.getOpposite()))//get all modules in the opposite face of dir.
                        if (module instanceof NetworkModule networkModule)
                            this.connect(networkModule);

        isFresh = true;
    }


    @Override
    public void writeData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("ID", ID);
    }

    @Override
    public void readData(CompoundTag tag, HolderLookup.Provider registries) {
        ID = tag.getInt("ID");
    }

    public void writeUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("ID", ID);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        ID = tag.getInt("ID");
    }

    @Override
    public void onDataPacket(Connection net, CompoundTag compound, HolderLookup.Provider lookupProvider) {
        handleUpdateTag(compound, lookupProvider);
    }

    @Override
    public int getID() {
        return ID;
    }

    @Override
    public void onLoad(ModulesHolder entity) {
        refresh(entity);
    }

    /// Returns a copy of children
    public NetworkModule[] getNeighbors() {
        return Arrays.copyOf(this.neighbors,this.neighbors.length);
    }

    public int getNeighborCount(){
        return neighbors.length;
    }

    /// Dirt the {@link Master} from any module in the network.
    public void dirtMaster(){
        if (root.master != null)
            root.master.dirt();
    }

    public @NotNull NetworkModule getRoot() {
        return root;
    }

    public Master getMaster() {
        return master;
    }

    /**
     * iterates in all network modules
     */
    @Override
    public @NotNull Iterator<NetworkModule> iterator() {
        HashSet<NetworkModule> visited = new HashSet<>();
        Queue<NetworkModule> queue = new ArrayDeque<>(List.of(this));

        for (;;)
        {
            // loop control
            NetworkModule next = queue.poll();
            if (next == null) break;
            if (visited.contains(next)) continue;
            visited.add(next);

            queue.addAll(Arrays.asList(next.neighbors));
        }
        return visited.iterator();
    }

    /// The master factory, you must create you own {@link Master} and instantiate where.
    public abstract Master createMaster();
}
