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

        nodes.add(root);
        Queue<Node> queue = new ArrayDeque<>(root.children);
        for (Node node : queue)
        {
            nodes.add(node);
            queue.addAll(node.children);
        }
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
        nodes.get(index).add(node);
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
        if (proxy.shouldHandle())
            return proxy.remove(index);

        Node node = nodes.set(index, null);

        //split the network
        for (Node child : node.children)
        {
            Network split = new Network(child);
            this.proxy.addAsMap(this.nodes.indexOf(child), split.getRootPointer());
            //todo finish implementation.
        }

        if (node == root)
            return node;

        //find the father and remove it from his children.
        {
            Stack<Node> path = GET_PATH(new Stack<>(), this.root, node);
            if (path == null)
                throw new RuntimeException("Can't remove the node \"%s\" because can't find a path to \"%s\"!".formatted(node.toString(), this.root.toString()));
            path.pop();//the top is the node it self.
            Node father = path.pop();
            father.children.remove(node);
        }
        
        return node;
    }

    void reRoot(Node newRoot)
    {
        Stack<Node> path = GET_PATH(new Stack<>(), this.root, newRoot);
        if (path == null)
            throw new RuntimeException("Can't re-root the network because can't find a path to \"%s\"!".formatted(newRoot.toString()));
        for (int i = 0; i < path.size()-1; i++)
        {
            var a = path.get(i);
            var b = path.get(i+1);
            a.children.remove(b);
            b.children.add(a);
        }
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
        List<Node.TickableNode> remain;
        if (root instanceof Node.TickableNode tickableNode)
            remain = new ArrayList<>(List.of(tickableNode));
        else
            return;

        int i = 0;
        while (i < remain.size())
        {
            Node.TickableNode node = remain.get(i);
            node.run();
            node.iterator().forEachRemaining(a -> {if (a instanceof Node.TickableNode t) remain.add(t);});
            i++;
        }
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

        for (Node child : root.children)
            if (GET_PATH(path, child, target) != null)
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
        to_node.children.add(from_node);
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
        CompoundTag main = tag.getCompound("net");
        for (String key : main.getAllKeys())
        {
            CompoundTag self = main.getCompound(key);

            UUID uuid = UUID.fromString(key);
            Node[] nodes = new Node[self.getInt("length")];

            Proxy proxy = Proxy.fromTag(self.getCompound("proxy"));
            Node root = Node.fromStringClass(self.getString("root_class"));
            root.fromTag(self.getCompound("root"), nodes);
            nodes[0] = root;
            Network net = new Network(root, new ArrayList<>(Arrays.asList((nodes))), uuid, proxy);
            net.availablePointers = self.getInt("availablePointers");

            NETWORK_LIST.put(uuid, net);
        }

        //assign the memory reference index in the proxys.
        for (Map.Entry<UUID, Network> entry : NETWORK_LIST.entrySet())
        {
            Network net = entry.getValue();
            if (net.proxy.shouldHandle())
            {
                var p_n = NETWORK_LIST.get(net.proxy.target_uuid);
                if (p_n == null)
                    throw new IllegalStateException("Network %s is trying to proxy a null network %s".formatted(net.network_index, net.proxy.target_uuid));
                net.proxy.network = p_n;
            }
        }
    }

    /// Save all data in the output stream and clear the network map if the clear flag is true.
    public static void SAVE_DATA(CompoundTag tag, boolean shouldClear) {
        CompoundTag main = new CompoundTag();
        for (Map.Entry<UUID, Network> entry : NETWORK_LIST.entrySet())
        {
            var net = entry.getValue();
            if (net.availablePointers == 0)//ignore non-referenced networks.
                continue;

            CompoundTag self = new CompoundTag();
            self.put("proxy", net.proxy.toTag());
            self.putString("root_class", net.root.getClass().getName());
            self.put("root", net.root.toTag(net.nodes));
            self.putInt("length",net.nodes.size());
            self.putInt("availablePointers",net.availablePointers);
            main.put(net.network_index.toString(), self);
        }

        tag.put("net",main);

        if (shouldClear)
            NETWORK_LIST.clear();
    }
}
