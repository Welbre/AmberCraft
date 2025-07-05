package welbre.ambercraft.sim.network;

import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.util.*;

import static welbre.ambercraft.sim.network.Network.*;

public class Network implements Iterable<Node> {
    private static final Map<UUID,Network> NETWORK_LIST = new HashMap<>();

    private final Node root;
    private final List<Node> nodes = new ArrayList<>();
    private final UUID network_index;

    public Network(Node root) {
        network_index = UUID.randomUUID();
        NETWORK_LIST.put(network_index,this);

        this.root = root;
        nodes.add(root);
    }

    /// Save all data in the output stream and clear the network map if the clear flag is true.
    public static void FLUSH_DATA(OutputStream writer, boolean shouldClear) {
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
