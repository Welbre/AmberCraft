package welbre.ambercraft.module.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.world.level.Level;
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
 * The {@link NetworkModule#masterLogic} is an important part of this module, they control all the behavior of the network and their modules,
 * only one instance is allowed in the network; therefore, only the master must have this field assigned.<br>
 * The master has a compilation system, so after the network change, you must re-compile the master using {@link Master#dirt()}.
 * The {@link NetworkModule#dirtMaster()} can be used to dirt the master from any node in the network.<br>
 *
 * The {@link NetworkModule#isMaster()} method can be used to check if the current Module is the master,
 * and {@link NetworkModule#createMaster()} should be overwritten to return your own instance of {@link Master}.<br>
 * The master is a filed that signatures the network, modules in the same network must agree about the master module, therefore, only one master exits in the network.
 *
 * If you are extending the class, {@link #isMaster()} == true if and only if {@link #masterLogic} != null, if this condition isn't true, the module is corrupted!
 * This check isn't made automatically, but {@link #checkInconsistencies()} can be used to do that and other integrity checks.
 *
 * @see Master
 */
public abstract class NetworkModule implements Module, Serializable, Iterable<NetworkModule> {
    /**
     * Points to the network master.<br>
     * Only one master is allowed in the network;
     * so, the field can be used to check if 2 nodes are in the same network easily.
     */
    protected @NotNull NetworkModule master = this;
    protected Master masterLogic = createMaster();
    protected NetworkModule[] neighbors = new NetworkModule[0];

    /// Defines if is the first time that the module is used.
    public boolean isFresh = false;
    public int ID = new Random().nextInt(0,0xffffff);

    public NetworkModule() {

    }

    private void setMaster()
    {
        if (master == this)//is already the master
            return;

        var oldMaster = this.master.masterLogic;
        for (NetworkModule module : this)
            module.master = this;

        this.masterLogic = oldMaster == null ? createMaster() : oldMaster;
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
                if (neighbor.isMaster())
                {
                    neighbor.dirtMaster();
                    continue;
                }
                List<NetworkModule> path = neighbor.getPath(this.master);
                if (path.isEmpty())//if after remotion, it is isolated, set as master, a split happens;
                {
                    for (NetworkModule module : neighbor)
                        module.master = neighbor;

                    neighbor.masterLogic = neighbor.createMaster();
                }
                else
                    neighbor.dirtMaster();
            }
        }

        this.neighbors = new NetworkModule[0];
        this.master = this;
        this.masterLogic = createMaster();
    }

    /**
     * Connects <code>this</code> module to <code>target</code>.<br>
     * This method is very friendly. If needed, they merge the network, re-master, and dirt all masters in the process.
     * @return if a new connection has been created.
     */
    public boolean connect(NetworkModule target)
    {
        if (this.master == target.master) //always in the same network
        {
            //check the same connection will be done again
            for (NetworkModule neighbor : this.neighbors)
                if (neighbor == target)// this already contains the target.
                    return false;
        } else {
            //at this point 2 different networks are connecting.
            //first, clear up the target network; these have your own master,
            //so, set the master to this.master (the master of this network), and clear up any master in the target network
            for (var nm : target)
            {
                nm.master = this.master;
                nm.masterLogic = null;
            }
        }

        // only now, add the target to this network.
        addNeighbor(target);
        target.addNeighbor(this);

        dirtMaster();//dirt the master to compile all changes
        return true;
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

    /// @return If the current module is the master of the network.
    public boolean isMaster()
    {
        return master == this;
    }

    /// used only in diagnostic, test, and debug.
    /// @return null if success, or an array of execution in the module.
    public RuntimeException[] checkInconsistencies() {
        List<RuntimeException> errors = new ArrayList<>();
        if (masterLogic != null && !isMaster())
            errors.add(new IllegalStateException("module 0x%x corrupted! has MasterLogic but isn't Master".formatted(this.ID)));
        if (masterLogic == null && isMaster())
            errors.add(new IllegalStateException("module 0x%x corrupted! is Master but hasn't MasterLogic".formatted(this.ID)));

        for (NetworkModule n : neighbors)
        {
            if (n.master != this.master)//check if the neighbor agrees about the master.
                errors.add(new IllegalStateException("module 0x%x corrupted! with master 0x%X don't agree with module 0x%X and its master 0x%X".formatted(this.ID, this.master.ID, n.ID, n.master.ID)));
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
    public void onLoad(ModulesHolder entity)
    {

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
        if (master.masterLogic != null)
            master.masterLogic.dirt();
    }

    public @NotNull NetworkModule getMaster() {
        return master;
    }

    public Master getMasterLogic() {
        return masterLogic;
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

    /// Used to allocate resources before the usage of the module.
    public abstract void alloc();
    /// Used to run code before break the module. mostly used to free resources/disconnect the network.
    public abstract void free();

    /// A module consumer that runs the alloc operation of any network module.
    public static <T extends ModulesHolder> void ALLOC_MODULE_CONSUMER(NetworkModule networkModule, T entity, Level level, BlockPos pos)
    {
        networkModule.alloc();
    }

    /// A module consumer that runs the free operation of any network module.
    public static <T extends ModulesHolder> void PRE_FREE_MODULE_CONSUMER(NetworkModule networkModule, T entity, Level level, BlockPos pos)
    {
        networkModule.free();
    }
}
