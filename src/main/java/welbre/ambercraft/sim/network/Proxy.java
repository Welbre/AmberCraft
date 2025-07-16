package welbre.ambercraft.sim.network;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class Proxy {
    public int pointers;
    public UUID target_uuid;
    public Network network;
    private int offSet;
    private final Map<Integer, Pointer<?>> addrMap = new HashMap<>();
    private boolean shouldHandle = false;

    Proxy(int offSet, Network network, UUID target_uuid, int pointers) {
        this.offSet = offSet;
        this.network = network;
        this.target_uuid = target_uuid;
        this.pointers = pointers;
        this.shouldHandle = true;
    }

    public Proxy(Network target) {
        setTarget(target);
    }

    public Proxy(){}

    public void setTarget(Network target){
        this.target_uuid = target.network_index;
        this.network = target;
        this.offSet = target.nodes.size();
        this.pointers = target.availablePointers;
        this.shouldHandle = true;
    }

    public void addAsMap(int index, Pointer<?> pointer){
        addrMap.put(index, pointer);
        shouldHandle = true;
    }

    public void removeAsMap(int index){
        addrMap.remove(index);
        shouldHandle = !addrMap.isEmpty();
    }

    public boolean shouldHandle(){
        return shouldHandle;
    }

    public Node get(int index) {
        Pointer<?> pointer = addrMap.get(index);
        if (pointer != null)
            return Network.GET_NODE(pointer);
        return network.getNode(index + offSet);
    }

    public <T extends Node> Pointer<T> addNode(T node, int index) {
        Pointer<?> pointer = addrMap.get(index);
        if (pointer != null)
            return Network.ADD_NODE(node, pointer);
        return network.addNode(node, index);
    }

    public Node remove(int index) {
        Pointer<?> pointer = addrMap.get(index);
        if (pointer != null)
            return Network.REMOVE(pointer);
        return network.remove(index + offSet);
    }

    public static Proxy fromTag(CompoundTag tag) {
        UUID id = tag.getUUID("id");
        return new Proxy(tag.getInt("off"), null, id, tag.getInt("p"));
    }

    public CompoundTag toTag() {
        var tag = new CompoundTag();
        tag.putInt("p", pointers);
        tag.putUUID("id", target_uuid);
        tag.putInt("off", offSet);
        return tag;
    }

    //Translate a pointer to a direct connection.
    public <T extends Node> Pointer<T> direct(Pointer<T> pointer) {
        return direct(pointer, this.network, offSet);
    }

    private <T extends Node> Pointer<T> direct(Pointer<T> pointer, Network network, int offSetStack)
    {
        if (network.proxy.shouldHandle())
        {
            offSetStack += network.proxy.offSet;
            return direct(pointer, network.proxy.network, offSetStack);
        }
        return new Pointer<>(network.network_index, pointer.index + offSetStack, pointer.aClass, pointer.isPersistent);
    }
}
