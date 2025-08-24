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

public class FacedCableComponent implements DataComponentType<FacedCableComponent>, Supplier<CableData> {
    private final byte typeIndex;
    private final CableData data;

    public FacedCableComponent(int typeIndex, CableData data) {
        this.typeIndex = (byte) typeIndex;
        this.data = data;
    }

    public FacedCableComponent(CableType type, int color) {
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
    public @Nullable Codec<FacedCableComponent> codec() {
        return CODEC;
    }

    @Override
    public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, FacedCableComponent> streamCodec() {
        return STREAM_CODEC;
    }

    public static final Codec<FacedCableComponent> CODEC = RecordCodecBuilder.create(
            ins ->
                    ins.group(
                            Codec.BYTE.fieldOf("index").forGetter(FacedCableComponent::getTypeIndex),
                            CableData.CODEC.fieldOf("data").forGetter(FacedCableComponent::getData)
                    ).apply(ins, FacedCableComponent::new)
    );

    public static final StreamCodec<ByteBuf, FacedCableComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, FacedCableComponent::getTypeIndex,
            CableData.STREAM_CODEC, FacedCableComponent::getData,
            FacedCableComponent::new
    );

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FacedCableComponent comp)
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
