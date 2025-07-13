package welbre.ambercraft.module.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.blockentity.HeatConductorBE;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModuleFactory;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.sim.heat.HeatNode;
import welbre.ambercraft.sim.network.Network;
import welbre.ambercraft.sim.network.Pointer;

public class HeatModule implements Module {
    Pointer<HeatNode> pointer;

    public HeatModule() {
    }

    public HeatNode getHeatNode(){
        return Network.GET_NODE(pointer);
    }

    /// Returns a copy of the pointer.
    public Pointer<HeatNode> getPointer() {
        return new Pointer<>(pointer);
    }

    @Override
    public void writeData(CompoundTag tag, HolderLookup.Provider registries) {
        if (pointer != null)
            tag.put("np", pointer.getAsTag());
    }

    @Override
    public void readData(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("np"))
            pointer = Pointer.GET_FROM_TAG(tag.getCompound("np"));
    }

    /**
     * Allocates a new instance of a {@link HeatNode} and registers it within the network,
     * @return the newly created and registered {@link HeatNode} instance.
     */
    public HeatNode alloc() {
        var x = new HeatNode();
        pointer = Network.CREATE(x);
        return x;
    }

    /**
     * Initialize the current module in the world, using the blockEntity, the level, and the position.<br>
     *
     * Server side only!
     */
    public <T extends BlockEntity & ModulesHolder> void init(T entity, ModuleFactory<HeatModule,T> factory, LevelAccessor level, BlockPos pos) {
        for (Direction dir : Direction.values())
        {
            BlockEntity relative = level.getBlockEntity(pos.relative(dir));
            if (relative instanceof ModulesHolder modular)
            {
                @NotNull HeatModule[] module = modular.getModule(HeatModule.class, dir.getOpposite());
                for (HeatModule heatModule : module)
                {
                    Network.CONNECT(pointer, heatModule.getPointer());
                }
            }
        }

        HeatNode self = Network.GET_NODE(pointer);
        if (self != null)
            self.setTemperature(HeatNode.GET_AMBIENT_TEMPERATURE(level, pos));
    }

    /**
     * Frees the pointer associated with this module.<br>
     * Before call this, the internal pointer will be null.
     */
    public void free() {
        try
        {
            Network.REMOVE(pointer);
            pointer.free();
            pointer = null;
        } catch (Pointer.InvalidNetwork e)
        {
            e.printStackTrace(System.err);
        }
    }
}
