package welbre.ambercraft.subblock;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
    public Map<@NotNull Direction, @Nullable TinyBlockState> fullOccluded = new EnumMap<>(Direction.class);
    /// Neighbors in each direction
    public Map<@NotNull Direction, List<TinyBlockState>> neighbors = new EnumMap<>(Direction.class);

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
        t.putByteArray("ext", externalContact.stream().map(d -> d == null ? (byte) -1 : (byte) d.ordinal()).toList());
        {
            CompoundTag occluded = new CompoundTag();
            this.fullOccluded.forEach((k,v) -> occluded.putInt(k.getName(), v == null ? -1 : v.getCompactedPosition()));
            t.put("occ", occluded);
        }
        {
            //store all states in the neighbors as hash, that will be recovered layer
            CompoundTag neighbors = new CompoundTag();
            this.neighbors.forEach((k,v) -> neighbors.putIntArray(k.getName(), v.stream().map(TinyBlockState::getCompactedPosition).toList()));
            t.put("nei", neighbors);
        }
        return t;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag tag)
    {
        var def = TinyBlockRegister.FROM_STRING(tag.getString("def"));
        if (def == null) throw new RuntimeException("Tried to deserialize a TinyBlockState with a null definition!");
        //clear up
        externalContact.clear();
        fullOccluded.clear();
        neighbors.clear();

        definition = def;
        x = tag.getShort("x");
        y = tag.getShort("y");
        z = tag.getShort("z");
        externalContact.addAll(Arrays.stream(ArrayUtils.toObject(tag.getByteArray("ext"))).map(by -> by == -1 ? null : Direction.values()[by]).toList());
        {
            CompoundTag occluded = tag.getCompound("occ");
            for (String key : occluded.getAllKeys())
            {
                Direction direction = Direction.byName(key);
                if (direction == null) throw new RuntimeException("Tried to deserialize a TinyBlockState neighbor map with a invalid direction (%s)!".formatted(key));

                SubBlockBE.TBSReference.SOLVE(occluded.getShort(key), v -> fullOccluded.put(direction, v));
            }
        }
        {
            CompoundTag neighbors = tag.getCompound("nei");
            for (String key : neighbors.getAllKeys())
            {
                Direction direction = Direction.byName(key);
                if (direction == null) throw new RuntimeException("Tried to deserialize a TinyBlockState neighbor map with a invalid direction (%s)!".formatted(key));

                int[] pos = neighbors.getIntArray(key);
                var solved = new ArrayList<TinyBlockState>(pos.length);
                this.neighbors.put(direction, solved);

                SubBlockBE.TBSReference.SOLVE(pos, solved::add);//finish the memory address of the object later.
            }
        }
    }

    /// Returns an integer with all x,y and z in one short, xxxxyyyyzzzz where x is the leftest bit.
    /// Notice that only the last 12 bits are used.
    public int getCompactedPosition()
    {
        return ((x & 0xF) << 8) | ((y & 0xF) << 4) | ((z & 0xF));
    }

    /// returns the AABB with the bounds translated
    protected @NotNull AABB getTranslatedBounds()
    {
        return definition.getTranslatedBounds(this);
    }

    /// Solves the shape ande return a translated list of AABB
    protected @NotNull VoxelShape getTranslatedShape()
    {
        return definition.getTranslatedShape(this);
    }

    /// Solves the shape ande return a translated list of AABB
    protected @NotNull List<AABB> getTranslatedAABB()
    {
        return definition.getTranslatedAABB(this);
    }

    /// A helper to add state in the map
    protected void addNeighbor(final Direction direction, TinyBlockState state)
    {
        List<TinyBlockState> states = neighbors.computeIfAbsent(direction, k -> new ArrayList<>());
        states.add(state);
    }
}
