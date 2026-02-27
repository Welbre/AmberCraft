package welbre.ambercraft.subblock;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Is the representation of {@link TinyBlock} in the SubBlock.<br>
 * Roughly, the TinyBlock is the behavior/definition of how the TinyBlockState will interact with the world and the others TinyBS in the Block.<br>
 * This class is mostly used in the internals of the mod, you should worry about the TinyBlock itself.
 */
public class TinyBlockState implements INBTSerializable<CompoundTag>
{
    public @NotNull TinyBlock definition;
    public short x,y,z;
    /// List with all directions that the state have external access (is in the edge of the subblock)
    public List<Direction> externalContact = new ArrayList<>();
    /**
     * stores all full occluded faces.<br>If it has the key, then value is the state that occlude at this direction or null if is occluded by block.
     */
    public Map<Direction, @Nullable TinyBlockState> fullOccluded = new EnumMap<>(Direction.class);
    /// Neighbors in each direction
    public Map<Direction, List<TinyBlockState>> neighbors = new EnumMap<>(Direction.class);

    /// <a color="red">USE ONLY FOR SERIALIZATION, AND INITIALIZE THE DEFINITION FIELD WITH A NOTNULL VALUE!</a>
    protected TinyBlockState()
    {
        //noinspection DataFlowIssue
        definition = null;//used only for serialization!
    }

    /// Default constructor initialized with a tiny block type and the position in the SubBlock
    public TinyBlockState(@NotNull TinyBlock tinyBlock, int x, int y, int z)
    {
        this.definition = tinyBlock;
        this.x = (short) x;
        this.y = (short) y;
        this.z = (short) z;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        var t = new CompoundTag();

        t.putString("def", definition.registerName.toString());
        t.putShort("x", x);
        t.putShort("y", y);
        t.putShort("z", z);
        //todo serialize the maps too
        return t;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag nbt)
    {
        var def = TinyBlockRegister.FROM_STRING(nbt.getString("def"));
        if (def == null) throw new RuntimeException("Tried to deserialize a TinyBlockState with a null definition!");
        definition = def;
        x = nbt.getShort("x");
        y = nbt.getShort("y");
        z = nbt.getShort("z");
        //todo serialize the maps too
    }

    /// returns the AABB with the bounds translated
    protected AABB getTranslatedBounds()
    {
        return definition.shape.bounds().move(x / 16.0, y / 16.0, z / 16.0);
    }

    /// A helper to add state in the map
    protected void addNeighbor(final Direction direction, TinyBlockState state)
    {
        List<TinyBlockState> states = neighbors.computeIfAbsent(direction, k -> new ArrayList<>());
        states.add(state);
    }
}
