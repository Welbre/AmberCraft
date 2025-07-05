package welbre.ambercraft.module;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import welbre.ambercraft.sim.heat.HeatNode;
import welbre.ambercraft.sim.network.Network;

public class HeatModule implements Module {
    Network.NPointer<HeatNode> pointer;

    public HeatModule(BlockEntity entity) {
        Level level;
        if ((level = Minecraft.getInstance().level) != null)
        {
            HeatNode node = new HeatNode(HeatNode.GET_AMBIENT_TEMPERATURE(level, entity.getBlockPos()));
            pointer = Network.CREATE(node);
        }
    }

    public HeatNode getHeatNode(){
        return Network.GET_NODE(pointer);
    }

    @Override
    public void writeData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("np", pointer.getAsTag());
    }

    @Override
    public void readData(CompoundTag tag, HolderLookup.Provider registries) {
        pointer = Network.NPointer.GET_FROM_TAG(tag.getCompound("np"));
    }

    public void free() {
        Network.REMOVE(pointer);
    }
}
