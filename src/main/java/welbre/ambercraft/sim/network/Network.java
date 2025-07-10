package welbre.ambercraft.sim.network;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static welbre.ambercraft.sim.network.Network.Node;

public class Network implements Iterable<Node> {
    private static final Map<UUID,Network> NETWORK_LIST = new HashMap<>();

    private Node root;
    private final List<Node> nodes;
    private final UUID network_index;
    private int availablePointers;
    private Proxy proxy;

    private Network(Node root,List<Node> nodes, UUID network_index) {
        this.root = root;
        this.network_index = network_index;
        this.nodes = nodes;
    }

    public Network(Node root) {
        network_index = UUID.randomUUID();
        NETWORK_LIST.put(network_index,this);

        this.root = root;
        this.nodes = new ArrayList<>();
        nodes.add(root);
    }

    public Node getRoot() {
        return root;
    }

    private <T extends Node> Pointer<T> addNode(T node, int index)
    {
        nodes.get(index).add(node);
        nodes.add(node);
        return new Pointer<>(network_index, nodes.size() - 1, (Class<T>) node.getClass(), true);
    }

    private Node getNode(int index)
    {
        if (proxy != null)
            return proxy.get(index);
        else
            return nodes.get(index);
    }

    private Node remove(int index)
    {
        Node node = nodes.remove(index);
        if (node != null)
        {
            Queue<Node> remains = new ArrayDeque<>(List.of(root));
            while (!remains.isEmpty())
            {
                Node poll = remains.poll();
                if (poll == node)
                {
                    poll.children.remove(node);
                    return node;
                } else
                    remains.addAll(poll.children);
            }
        }
        return node;
    }

    public <T extends Node> Pointer<T> getNodePointer(T node)
    {
        for (int i = 0; i < nodes.size(); i++)
            if (Objects.equals(node, nodes.get(i)))
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
        List<TickableNode> remain;
        if (root instanceof TickableNode tickableNode)
            remain = new ArrayList<>(List.of(tickableNode));
        else
            return;

        int i = 0;
        while (i < remain.size())
        {
            TickableNode node = remain.get(i);
            node.run();
            node.iterator().forEachRemaining(a -> {if (a instanceof TickableNode t) remain.add(t);});
            i++;
        }
    }

    //---------------------------------------------------------------------------------------------------------------\\
    //-------------------------------------------Static methods------------------------------------------------------\\
    //---------------------------------------------------------------------------------------------------------------\\

    public static <T extends Node> Pointer<T> ADD_NODE(T node, Pointer<?> pointer)
    {
        Network network = NETWORK_LIST.get(pointer.netAddr);
        if (network == null)
            throw new Pointer.InvalidNetwork(pointer);
        return network.addNode(node, pointer.index);
    }

    public static <T extends Node> T REMOVE(Pointer<T> pointer)
    {
        Network network = NETWORK_LIST.get(pointer.netAddr);
        if (network == null)
            throw new Pointer.InvalidNetwork(pointer);


        Node node = network.remove(pointer.index);

        if (pointer.aClass.isInstance(node))
        {
            if (network.getNodes().isEmpty())//remove if the network is empty
                NETWORK_LIST.remove(pointer.netAddr);
            return pointer.aClass.cast(node);
        }
        return null;
    }

    public static <T extends Node> T GET_NODE(Pointer<T> pointer)
    {
        Network network = NETWORK_LIST.get(pointer.netAddr);
        if (network == null)
            throw new Pointer.InvalidNetwork(pointer);

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

    /// Merge the first network in the second, connecting the "from" in the "to" nodes.
    public static void CONNECT(Pointer<?> to, Pointer<?> from)
    {
        Network first = GET_NETWORK(to);
        Network second = GET_NETWORK(from);
        if (first == second)
            return;
        Node from_node = second.getNode(from.index);

        //make the "from" the root in second.
        {
            Stack<Node> path = GET_PATH(new Stack<>(), second.root, from_node);
            if (path == null)
                throw new RuntimeException("Can't found the path to \"from%s\" node in second network".formatted(from.toString()));
            for (int i = 0; i < path.size()-1; i++)
            {
                var a = path.get(i);
                var b = path.get(i+1);
                a.children.remove(b);
                b.children.add(a);
            }
        }

        //Put the nodes in the list.
        second.proxy = new Proxy(first);
        first.nodes.addAll(second.nodes);
        second.nodes.clear();
        second.root = null;

        //make the connection in the tree
        Node to_node = first.getNode(to.index);
        to_node.children.add(from_node);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Node> Pointer<T> CREATE(T root){
        Network network = new Network(root);
        return (Pointer<T>) new Pointer<>(network.network_index, 0, root.getClass(), true);
    }

    public static void TICK_ALL() {
        Network.NETWORK_LIST.forEach((id,net) -> net.tick());
    }

    public static void LOAD_DATA(CompoundTag tag)
    {
        CompoundTag main = tag.getCompound("net");
        for (String key : main.getAllKeys())
        {
            CompoundTag self = main.getCompound(key);

            UUID uuid = UUID.fromString(key);
            Node[] nodes = new Node[self.getInt("length")];
            Network net;

            if (self.contains("proxy"))
            {
                net = new Network(null, new ArrayList<>(), uuid);
                net.proxy = Proxy.fromTag(self.getCompound("proxy"));
            }
            else
            {
                Node root = Node.fromStringClass(self.getString("root_class"));
                root.fromTag(self.getCompound("root"), nodes);
                nodes[0] = root;
                net = new Network(root, new ArrayList<>(Arrays.asList((nodes))), uuid);
            }

            NETWORK_LIST.put(uuid, net);
        }

        //assign the memory reference index in the proxys.
        for (Map.Entry<UUID, Network> entry : NETWORK_LIST.entrySet())
        {
            Network net = entry.getValue();
            if (net.proxy != null)
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
            CompoundTag self = new CompoundTag();
            if (net.proxy != null)
                self.put("proxy", net.proxy.toTag());
            else
            {
                self.putString("root_class", net.root.getClass().getName());
                self.put("root", net.root.toTag(net.nodes));
                self.putInt("length",net.nodes.size());
            }
            main.put(net.network_index.toString(), self);
        }

        tag.put("net",main);

        if (shouldClear)
            NETWORK_LIST.clear();
    }

    //---------------------------------------------------------------------------------------------------------------\\
    //--------------------------------------------Extra classes------------------------------------------------------\\
    //---------------------------------------------------------------------------------------------------------------\\


    public static class Node implements Iterable<Node> {
        private final List<Node> children = new ArrayList<>();

        public Node() {
        }

        protected void add(Node node){
            children.add(node);
        }

        @Override
        public @NotNull Iterator<Node> iterator() {
            return children.iterator();
        }

        public CompoundTag toTag(List<Node> nodes) {
            var tag = new CompoundTag();
            var node_tag = new CompoundTag();
            for (int i = 0; i < children.size(); i++)
            {
                var self = children.get(i);
                var child = new CompoundTag();
                child.putString("cl",self.getClass().getName());
                child.put("ch", self.toTag(nodes));
                child.putInt("i", nodes.indexOf(self));

                node_tag.put("c"+i, child);
            }
            tag.put("node_tag", node_tag);
            return tag;
        }

        public Node fromTag(CompoundTag tag, Node[] nodes){
            var node_tag = tag.getCompound("node_tag");
            for (String key : node_tag.getAllKeys())
            {
                CompoundTag dataChild = node_tag.getCompound(key);

                Node child = Node.fromStringClass(dataChild.getString("cl"));
                child.fromTag(dataChild.getCompound("ch"), nodes);
                int idx = dataChild.getInt("i");
                nodes[idx] = child;

                this.add(child);
            }

            return this;
        }

        public static Node fromStringClass(String name){
            try
            {
                Class<?> aClass = Class.forName(name);
                if (Node.class.isAssignableFrom(aClass))
                    return (Node) aClass.getDeclaredConstructor().newInstance();
                else
                    throw new IllegalArgumentException("Class %s isn't a Node class!".formatted(aClass.getName()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static abstract class TickableNode extends Node implements Runnable{}

    public static class Pointer<T extends Node>{
        private final UUID netAddr;
        private final int index;
        private final Class<T> aClass;
        private final boolean isPersistent;

        private Pointer(UUID netAddr, int index, Class<T> aClass, boolean isPersistent)
        {
            this.netAddr = netAddr;
            this.index = index;
            this.aClass = aClass;
            this.isPersistent = isPersistent;
            if (isPersistent)
            {
                Network network = NETWORK_LIST.get(netAddr);
                network.availablePointers++;
            }
        }

        public Pointer(Pointer<T> pointer) {
            this(pointer.netAddr, pointer.index, pointer.aClass, false);
        }

        public CompoundTag getAsTag()
        {
            if (!isPersistent)
                throw new IllegalArgumentException("Trying to save a volatile pointer");

            CompoundTag tag = new CompoundTag();
            tag.putLong("id_m", netAddr.getMostSignificantBits());
            tag.putLong("id_l", netAddr.getLeastSignificantBits());
            tag.putInt("idx", index);
            tag.putString("class", aClass.getName());
            return tag;
        }

        public static <T extends Node> Pointer<T> GET_FROM_TAG(CompoundTag tag)
        {
            final long most = tag.getLong("id_m");
            final long least = tag.getLong("id_l");
            final int idx = tag.getInt("idx");
            Class<?> bClass;
            try
            {
                bClass = Class.forName(tag.getString("class"));
            } catch (ClassNotFoundException e)
            {
                throw new RuntimeException(e);
            }
            var pointer = new Pointer<>(new UUID(most, least), idx, (Class<T>) bClass, true);
            //check if is pointing to a proxied network, if true, then return the direct address.
            try {
                Network network = Network.GET_NETWORK(pointer);
                if (network.proxy != null)
                    pointer = network.proxy.direct(pointer);
            } catch (Exception ignore){}

            return pointer;
        }

        public void free(){
            if (isPersistent)
            {
                Network network = NETWORK_LIST.get(netAddr);
                network.availablePointers--;
            }
        }

        @Override
        public String toString() {
            return String.valueOf(netAddr.toString().toCharArray(), 0, 8) + "#" + aClass.getSimpleName() + ":" + index;
        }

        public static final class InvalidNetwork extends RuntimeException {

            public InvalidNetwork(Pointer<?> pointer) {
                super("Invalid pointer, network(%s) not found!".formatted(pointer.netAddr.toString()));
            }
        }
    }

    private static final class Proxy {
        public int pointers;
        public UUID target_uuid;
        public Network network;
        private final int offSet;

        private Proxy(int offSet, Network network, UUID target_uuid, int pointers) {
            this.offSet = offSet;
            this.network = network;
            this.target_uuid = target_uuid;
            this.pointers = pointers;
        }

        public Proxy(Network target) {
            this.target_uuid = target.network_index;
            this.network = target;
            this.offSet = target.nodes.size();
            this.pointers = target.availablePointers;
        }

        public Node get(int index)
        {
            return network.getNode(index + offSet);
        }

        public CompoundTag toTag() {
            var tag = new CompoundTag();
            tag.putInt("p", pointers);
            tag.putUUID("id", target_uuid);
            tag.putInt("off", offSet);
            return tag;
        }

        public static Proxy fromTag(CompoundTag tag){
            UUID id = tag.getUUID("id");
            return new Proxy(tag.getInt("off"), null, id, tag.getInt("p"));
        }

        //Translate a pointer to a direct connection.
        <T extends Node> Pointer<T> direct(Pointer<T> pointer) {
            return new Pointer<>(target_uuid, pointer.index + offSet, pointer.aClass, pointer.isPersistent);
        }
    }
}
