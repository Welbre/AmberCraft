package welbre.ambercraft.subblock;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

/**
 * Is the representation of {@link TinyBlock} in the SubBlock.<br>
 * Roughly, the TinyBlock is the behavior/definition of how the TinyBlockState will interact with the world and the others TinyBS in the Block.<br>
 * This class is mostly used in the internals of the mod, you should worry about the TinyBlock itself.
 */
public class TinyBlockState implements INBTSerializable<CompoundTag>
{
    public @NotNull TinyBlock definition;
    public short x,y,z;

    /// <a color="red">USE ONLY FOR SERIALIZATION, AND INITIALIZE THE DEFINITION FIELD WITH A NOTNULL VALUE!</a>
    protected TinyBlockState()
    {
        //noinspection DataFlowIssue
        definition = null;//used only for serialization!
    }

    protected TinyBlockState(@NotNull TinyBlock tinyBlock, int x, int y, int z)
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
    }
}
