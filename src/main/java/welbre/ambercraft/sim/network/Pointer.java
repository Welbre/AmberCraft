package welbre.ambercraft.sim.network;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class Pointer<T extends Node> {
    final UUID netAddr;
    final int index;
    final Class<T> aClass;
    final boolean isPersistent;

    Pointer(UUID netAddr, int index, Class<T> aClass, boolean isPersistent) {
        this.netAddr = netAddr;
        this.index = index;
        this.aClass = aClass;
        this.isPersistent = isPersistent;
        if (isPersistent)
        {
            Network network = Network.NETWORK_LIST.get(netAddr);
            network.availablePointers++;
        }
    }

    public Pointer(Pointer<T> pointer) {
        this(pointer.netAddr, pointer.index, pointer.aClass, false);
    }

    public CompoundTag getAsTag() {
        if (!isPersistent)
            throw new IllegalArgumentException("Trying to save a volatile pointer");

        CompoundTag tag = new CompoundTag();
        tag.putLong("id_m", netAddr.getMostSignificantBits());
        tag.putLong("id_l", netAddr.getLeastSignificantBits());
        tag.putInt("idx", index);
        tag.putString("class", aClass.getName());
        return tag;
    }

    public static <T extends Node> Pointer<T> GET_FROM_TAG(CompoundTag tag) {
        final long most = tag.getLong("id_m");
        final long least = tag.getLong("id_l");
        final int idx = tag.getInt("idx");
        Class<?> bClass;
        Network network;
        try
        {
            bClass = Class.forName(tag.getString("class"));
            assert bClass.isAssignableFrom(Node.class);
            Pointer<?> pointer = new Pointer<>(new UUID(most, least), idx, (Class<T>) bClass, false);
            network = Network.GET_NETWORK(pointer);
        } catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }

        Pointer<T> pointer = new Pointer<>(new UUID(most, least), idx, (Class<T>) bClass, true);
        //check if is pointing to a proxied network, if true, then return the direct address.
        if (network.proxy != null)
        {
            pointer.free();
            return network.proxy.direct(pointer);
        } else
            return pointer;
    }

    public void free() {
        if (isPersistent)
        {
            Network network = Network.NETWORK_LIST.get(netAddr);
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
