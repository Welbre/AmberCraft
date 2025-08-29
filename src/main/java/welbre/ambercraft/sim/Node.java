package welbre.ambercraft.sim;

import net.minecraft.nbt.CompoundTag;

public class Node {

    public Node() {
    }

    public CompoundTag toTag() {
        var tag = new CompoundTag();
        tag.putString("class", this.getClass().getName());
        return tag;
    }

    public Node fromTag(CompoundTag tag) {
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
}
