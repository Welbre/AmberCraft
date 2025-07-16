package welbre.ambercraft.sim.network;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Network implements Iterable<Node> {
    protected static final Map<UUID,Network> NETWORK_LIST = new HashMap<>();

    protected Node root;
    protected final List<Node> nodes;
    protected final UUID network_index;
    protected int availablePointers;
    final @NotNull Proxy proxy;

    private Network(Node root, List<Node> nodes, UUID network_index, @NotNull Proxy proxy) {
        this.root = root;
        this.network_index = network_index;
        this.nodes = nodes;
        this.proxy = proxy;
    }

    public Network(Node root) {
        network_index = UUID.randomUUID();
        NETWORK_LIST.put(network_index,this);

        this.root = root;
        this.nodes = new ArrayList<>();
        this.proxy = new Proxy();
    }

    @Deprecated
    public Network getFinalPoint(){
        if (proxy.shouldHandle())
            return proxy.network.getFinalPoint();
        else
            return this;
    }

    public Node getRoot() {
        if (proxy.shouldHandle())
            return proxy.network.getRoot();
        else
            return root;
    }

    <T extends Node> Pointer<T> addNode(T node, int index)
    {
        if (proxy.shouldHandle())
            return proxy.addNode(node, index);
        nodes.add(node);
        //noinspection unchecked
        return new Pointer<>(network_index, nodes.size() - 1, (Class<T>) node.getClass(), true);
    }

    Node getNode(int index)
    {
        if (proxy.shouldHandle())
            return proxy.get(index);
        else
            return nodes.get(index);
    }

    Node remove(int index)
    {
        return null;
    }

    void reRoot(Node newRoot)
    {

    }

    public <T extends Node> Pointer<T> getNodePointer(T node)
    {
        for (int i = 0; i < nodes.size(); i++)
            if (Objects.equals(node, nodes.get(i)))
                //noinspection unchecked
                return new Pointer<T>(network_index, i, (Class<T>) node.getClass(), false);
        return null;
    }

    public List<Node> getNodes(){
        return nodes;
    }

    @Override
    public @NotNull Iterator<Node> iterator() {
        return getNodes().iterator();
    }

    public Pointer<?> getRootPointer()
    {
        return new Pointer<>(network_index, 0, root.getClass(), false);
    }


    public void tick(){

    }

    //---------------------------------------------------------------------------------------------------------------\\
    //-------------------------------------------Static methods------------------------------------------------------\\
    //---------------------------------------------------------------------------------------------------------------\\

    public static <T extends Node> Pointer<T> ADD_NODE(T node, Pointer<?> pointer)
    {
        Network network = GET_NETWORK(pointer);

        return network.addNode(node, pointer.index);
    }

    public static <T extends Node> T REMOVE(Pointer<T> pointer)
    {
        Network network = GET_NETWORK(pointer);

        Node node = network.remove(pointer.index);

        if (pointer.aClass.isInstance(node))
            return pointer.aClass.cast(node);
        return null;
    }

    public static <T extends Node> T GET_NODE(Pointer<T> pointer)
    {
        Network network = GET_NETWORK(pointer);

        Node node = network.getNode(pointer.index);
        if (pointer.aClass.isInstance(node))
            return pointer.aClass.cast(node);
        return null;
    }
    public static Network GET_NETWORK(Pointer<?> pointer)
    {
        Network network = NETWORK_LIST.get(pointer.netAddr);
        if (network == null)
            throw new Pointer.InvalidNetwork(pointer);
        return network;
    }

    private static Stack<Node> GET_PATH(Stack<Node> path, Node root, Node target){
        if (root == null)
            return null;

        path.add(root);
        if (path.peek() == target)
            return path;


        path.pop();
        return null;
    }

    /// Merge the first network in the second, connecting in a performatic way.
    public static void CONNECT(Pointer<?> nodeA, Pointer<?> nodeB)
    {
        Network first = GET_NETWORK(nodeA).getFinalPoint();
        Network second = GET_NETWORK(nodeB).getFinalPoint();


        //sort to the second be the sortest network
        if (first.nodes.size() < second.nodes.size())
        {
            Network temp = first;
            first = second;
            second = temp;
            Pointer<?> temp_p = nodeA;
            nodeA = nodeB;
            nodeB = temp_p;
        }

        if (first == second)
            return;

        //check cyclical connection via proxy.
        {
            Stack<Proxy> stack = new Stack<>();
            if (first.proxy.shouldHandle()) stack.add(first.proxy);
            if (second.proxy.shouldHandle()) stack.add(second.proxy);

            while (!stack.isEmpty())
            {
                Proxy next = stack.pop();
                if (next.network == second)
                    return;
                else
                    if (next.network.proxy.shouldHandle())
                        stack.add(next.network.proxy);
            }
        }
        Node from_node = second.getNode(nodeB.index);

        //make the "nodeB" the root in second.
        second.reRoot(from_node);

        //Put the nodes in the list.
        second.proxy.setTarget(first);
        first.nodes.addAll(second.nodes);
        second.nodes.clear();
        second.root = null;

        //make the connection in the tree
        Node to_node = first.getNode(nodeA.index);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Node> Pointer<T> CREATE(T root){
        Network network = new Network(root);
        return (Pointer<T>) new Pointer<>(network.network_index, 0, root.getClass(), true);
    }

    public static void TICK_ALL() {
        for (Map.Entry<UUID, Network> networkEntry : Network.NETWORK_LIST.entrySet())
        {
            networkEntry.getValue().tick();
        }
    }

    public static void LOAD_DATA(CompoundTag tag)
    {

    }

    /// Save all data in the output stream and clear the network map if the clear flag is true.
    public static void SAVE_DATA(CompoundTag tag, boolean shouldClear) {

    }
}
