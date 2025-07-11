package welbre.ambercraft.module.heat;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.sim.heat.HeatNode;
import welbre.ambercraft.sim.network.Network;
import welbre.ambercraft.sim.network.Network.NPointer;

public class HeatModule implements Module {
    NPointer<HeatNode> pointer;

    public HeatModule() {
    }

    public HeatNode getHeatNode(){
        return Network.GET_NODE(pointer);
    }

    /// Returns a copy of the pointer.
    public NPointer<HeatNode> getPointer() {
        return new NPointer<>(pointer);
    }

    @Override
    public void writeData(CompoundTag tag, HolderLookup.Provider registries) {
        if (pointer != null)
            tag.put("np", pointer.getAsTag());
    }

    @Override
    public void readData(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("np"))
            pointer = NPointer.GET_FROM_TAG(tag.getCompound("np"));
    }

    public HeatNode alloc() {
        var x = new HeatNode();
        pointer = Network.CREATE(x);
        return x;
    }

    public void free() {
        try
        {
            Network.REMOVE(pointer);
            pointer = null;
        } catch (NPointer.InvalidNetwork e)
        {
            e.printStackTrace(System.err);
        }
    }
}
