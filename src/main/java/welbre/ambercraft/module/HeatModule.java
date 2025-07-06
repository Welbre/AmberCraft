package welbre.ambercraft.module;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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
        tag.put("np", pointer.getAsTag());
    }

    @Override
    public void readData(CompoundTag tag, HolderLookup.Provider registries) {
        pointer = NPointer.GET_FROM_TAG(tag.getCompound("np"));
    }

    @Override
    public void alloc() {
        pointer = Network.CREATE(new HeatNode());
    }

    @Override
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
