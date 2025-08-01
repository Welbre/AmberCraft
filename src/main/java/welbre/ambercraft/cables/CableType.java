package welbre.ambercraft.cables;

import net.minecraft.client.resources.model.Material;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.blockentity.FacedCableBE;
import welbre.ambercraft.module.Module;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class CableType {
    private static final List<CableType> CABLE_TYPE_LIST = new ArrayList<>();
    public final byte cable_type_index;

    public CableType() {
        cable_type_index = (byte) (CABLE_TYPE_LIST.size() & 0xFF);
        CABLE_TYPE_LIST.add(this);
    }

    public static @NotNull CableType FromCableTypeIndex(byte index){
        var type = CABLE_TYPE_LIST.get(index);
        if (type == null)
            throw new IllegalArgumentException("The index %d is invalid!".formatted(index));
        return type;
    }

    public abstract double getWidth();
    public abstract double getHeight();
    public abstract Material getInsulationMaterial();
    public abstract Material getCableMaterial();
    /**
     * The cable type system is a single byte value that defines if a cable can connect with another of the same type.<br>
     * Ex: The electric cable has type 0, so this cable can connect only to other cables with type 0.<br>
     * Therefore, an electric(0) cable can't connect to a heat(1) cable, and a redstone cable can't connect either.<br>
     */
    public abstract byte getType();
    public abstract Module[] createModules(FacedCableBE entity);

    public enum Types implements Supplier<Byte> {
        ELECTRIC(0),
        HEAT(1),
        REDSTONE(2);
        private final byte type;

        Types(int type) {
            this.type = (byte) type;
        }

        @Override
        public Byte get() {
            return type;
        }
    }
}
