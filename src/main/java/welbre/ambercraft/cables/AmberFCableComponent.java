package welbre.ambercraft.cables;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class AmberFCableComponent implements DataComponentType<AmberFCableComponent>, Supplier<CableData> {
    private final byte typeIndex;
    private final CableData data;

    public AmberFCableComponent(int typeIndex, CableData data) {
        this.typeIndex = (byte) typeIndex;
        this.data = data;
    }

    public AmberFCableComponent(CableType type, int color) {
        this(type.cable_type_index,new CableData(color,type.getType(),false));
    }

    @Override
    public CableData get() {
        return new CableData(data);
    }

    public CableType getType(){
        return CableType.FromCableTypeIndex(typeIndex);
    }

    private byte getTypeIndex() {
        return typeIndex;
    }

    private CableData getData() {
        return data;
    }

    @Override
    public @Nullable Codec<AmberFCableComponent> codec() {
        return CODEC;
    }

    @Override
    public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, AmberFCableComponent> streamCodec() {
        return STREAM_CODEC;
    }

    public static final Codec<AmberFCableComponent> CODEC = RecordCodecBuilder.create(
            ins ->
                    ins.group(
                            Codec.BYTE.fieldOf("index").forGetter(AmberFCableComponent::getTypeIndex),
                            CableData.CODEC.fieldOf("data").forGetter(AmberFCableComponent::getData)
                    ).apply(ins, AmberFCableComponent::new)
    );

    public static final StreamCodec<ByteBuf, AmberFCableComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, AmberFCableComponent::getTypeIndex,
            CableData.STREAM_CODEC, AmberFCableComponent::getData,
            AmberFCableComponent::new
    );

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AmberFCableComponent comp)
        {
            if (comp.typeIndex != this.typeIndex)
                return false;
            return comp.data.equals(this.data);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
