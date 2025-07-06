package welbre.ambercraft.sim.network;

import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static welbre.ambercraft.sim.network.Network.Node;

public class Network implements Iterable<Node> {
    private static final Map<UUID,Network> NETWORK_LIST = new HashMap<>();

    private final Node root;
    private final List<Node> nodes;
    private final UUID network_index;

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

    public static void LOAD_DATA(CompoundTag tag)
    {
        CompoundTag main = tag.getCompound("net");
        for (String key : main.getAllKeys())
        {
            CompoundTag self = main.getCompound(key);
            ArrayList<Node> nodes = new ArrayList<>();
            UUID uuid = UUID.fromString(key);
            Node root = Node.fromStringClass(self.getString("root_class"));
            root.fromTag(self.getCompound("root"),nodes);

            nodes.add(root);
            Network net = new Network(root, nodes, uuid);
            NETWORK_LIST.put(uuid, net);
        }
    }

    /// Save all data in the output stream and clear the network map if the clear flag is true.
    public static void SAVE_DATA(CompoundTag tag, boolean shouldClear) {
        CompoundTag main = new CompoundTag();
        for (Map.Entry<UUID, Network> entry : NETWORK_LIST.entrySet())
        {
            var net = entry.getValue();
            CompoundTag self = new CompoundTag();
            self.putString("root_class", net.root.getClass().getName());
            self.put("root", net.root.toTag(net.nodes));

            main.put(net.network_index.toString(), self);
        }

        tag.put("net",main);

        if (shouldClear)
            NETWORK_LIST.clear();
    }

    public Node getRoot() {
        return root;
    }

    private <T extends Node> NPointer<T> addNode(T node, int index)
    {
        nodes.get(index).add(node);
        nodes.add(node);
        return new NPointer<>(network_index, nodes.size() - 1, (Class<T>) node.getClass());
    }

    private Node getNode(int index)
    {
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

    public <T extends Node> NPointer<T> getNodePointer(T node)
    {
        for (int i = 0; i < nodes.size(); i++)
            if (Objects.equals(node, nodes.get(i)))
                    return new NPointer<T>(network_index, i, (Class<T>) node.getClass());
        return null;
    }

    public List<Node> getNodes(){
        return nodes;
    }

    @Override
    public @NotNull Iterator<Node> iterator() {
        return getNodes().iterator();
    }

    public NPointer<?> getRootNPointer()
    {
        return new NPointer<>(network_index, 0, root.getClass());
    }

    public final void tick(){
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

    public static <T extends Node> NPointer<T> ADD_NODE(T node, NPointer<?> pointer)
    {
        Network network = NETWORK_LIST.get(pointer.netAddr);
        if (network == null)
            throw new NPointer.InvalidNetwork(pointer);
        return network.addNode(node, pointer.index);
    }

    public static <T extends Node> NPointer<T> ADD_NODE(NPointer<T> n_1, NPointer<T> n_2)
    {
        throw new NotImplementedException();
    }

    public static <T extends Node> T REMOVE(NPointer<T> pointer)
    {
        Network network = NETWORK_LIST.get(pointer.netAddr);
        if (network == null)
            throw new NPointer.InvalidNetwork(pointer);
        Node node = network.remove(pointer.index);
        if (pointer.aClass.isInstance(node))
        {
            if (network.getNodes().isEmpty())//remove if the network is empty
                NETWORK_LIST.remove(pointer.netAddr);
            return pointer.aClass.cast(node);
        }
        return null;
    }

    public static <T extends Node> T GET_NODE(NPointer<T> pointer)
    {
        Network network = NETWORK_LIST.get(pointer.netAddr);
        if (network == null)
            throw new NPointer.InvalidNetwork(pointer);

        Node node = network.getNode(pointer.index);
        if (pointer.aClass().isInstance(node))
            return pointer.aClass.cast(node);
        return null;
    }
    public static Network GET_NETWORK(NPointer<?> pointer)
    {
        Network network = NETWORK_LIST.get(pointer.netAddr);
        if (network == null)
            throw new NPointer.InvalidNetwork(pointer);
        return network;
    }
    @SuppressWarnings("unchecked")
    public static <T extends Node> NPointer<T> CREATE(T root){
        Network network = new Network(root);
        return (NPointer<T>) network.getRootNPointer();
    }

    public static void TICK_ALL() {
        Network.NETWORK_LIST.forEach((id,net) -> net.tick());
    }

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
                var child = new CompoundTag();
                child.putString("cl",children.get(i).getClass().getName());
                child.put("ch", children.get(i).toTag(nodes));
                child.putInt("i", nodes.indexOf(this));

                node_tag.put("c"+i, child);
            }
            tag.put("node_tag", node_tag);
            return tag;
        }

        public Node fromTag(CompoundTag tag, List<Node> nodes){
            var node_tag = tag.getCompound("node_tag");
            for (String key : node_tag.getAllKeys())
            {
                CompoundTag dataChild = node_tag.getCompound(key);

                Node child = Node.fromStringClass(dataChild.getString("cl"));
                child.fromTag(dataChild.getCompound("ch"), nodes);
                int idx = dataChild.getInt("i");
                nodes.set(idx, child);

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

    public record NPointer<T extends Node>(UUID netAddr, int index, Class<T> aClass) {
        public <K extends Node> NPointer(NPointer<K> pointer) {
            this(pointer.netAddr, pointer.index, (Class<T>) pointer.aClass);
        }

        public CompoundTag getAsTag()
        {
            CompoundTag tag = new CompoundTag();
            tag.putLong("id_m", netAddr.getMostSignificantBits());
            tag.putLong("id_l", netAddr.getLeastSignificantBits());
            tag.putInt("idx", index);
            tag.putString("class", aClass.getName());
            return tag;
        }

        public static <T extends Node> NPointer<T> GET_FROM_TAG(CompoundTag tag)
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
            return new NPointer<>(new UUID(most,least),idx, (Class<T>) bClass);
        }

        public static final class InvalidNetwork extends RuntimeException {

            public InvalidNetwork(NPointer<?> pointer) {
                super("Invalid pointer, network(%s) not found!".formatted(pointer.netAddr.toString()));
            }
        }
    }
}
