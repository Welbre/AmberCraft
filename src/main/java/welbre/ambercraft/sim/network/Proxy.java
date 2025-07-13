package welbre.ambercraft.sim.network;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

final class Proxy {
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

    public Node get(int index) {
        return network.getNode(index + offSet);
    }

    public CompoundTag toTag() {
        var tag = new CompoundTag();
        tag.putInt("p", pointers);
        tag.putUUID("id", target_uuid);
        tag.putInt("off", offSet);
        return tag;
    }

    public <T extends Node> Pointer<T> addNode(T node, int index) {
        return network.addNode(node, index);
    }

    public Node remove(int index) {
        return network.remove(index + offSet);
    }

    public static Proxy fromTag(CompoundTag tag) {
        UUID id = tag.getUUID("id");
        return new Proxy(tag.getInt("off"), null, id, tag.getInt("p"));
    }

    //Translate a pointer to a direct connection.
    <T extends Node> Pointer<T> direct(Pointer<T> pointer) {
        return direct(pointer, this.network, offSet);
    }

    private <T extends Node> Pointer<T> direct(Pointer<T> pointer, Network network, int offSetStack)
    {
        if (network.proxy != null)
        {
            offSetStack += network.proxy.offSet;
            return direct(pointer, network.proxy.network, offSetStack);
        }
        return new Pointer<>(network.network_index, pointer.index + offSetStack, pointer.aClass, pointer.isPersistent);
    }
}
