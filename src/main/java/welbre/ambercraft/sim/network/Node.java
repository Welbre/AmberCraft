package welbre.ambercraft.sim.network;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Node implements Iterable<Node> {
    final List<Node> children = new ArrayList<>();

    public Node() {
    }

    protected void add(Node node) {
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
            child.putString("cl", self.getClass().getName());
            child.put("ch", self.toTag(nodes));
            child.putInt("i", nodes.indexOf(self));

            node_tag.put("c" + i, child);
        }
        tag.put("node_tag", node_tag);
        return tag;
    }

    public Node fromTag(CompoundTag tag, Node[] nodes) {
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

    public static Node fromStringClass(String name) {
        try
        {
            Class<?> aClass = Class.forName(name);
            if (Node.class.isAssignableFrom(aClass))
                return (Node) aClass.getDeclaredConstructor().newInstance();
            else
                throw new IllegalArgumentException("Class %s isn't a Node class!".formatted(aClass.getName()));
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static abstract class TickableNode extends Node implements Runnable{}
}
