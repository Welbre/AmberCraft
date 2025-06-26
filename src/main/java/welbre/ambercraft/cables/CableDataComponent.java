package welbre.ambercraft.cables;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

public record CableDataComponent(int color, byte type, short packed_size) implements DataComponentType<CableDataComponent> {
    public static final short DEFAULT_PACKED_SIZE = PACK_SIZE(.4,.4/2);

    public CableDataComponent(int color, byte type) {
        this(color, type, DEFAULT_PACKED_SIZE);
    }

    public static final Codec<CableDataComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("color").forGetter(CableDataComponent::color),
                    Codec.BYTE.fieldOf("type").forGetter(CableDataComponent::type),
                    Codec.SHORT.fieldOf("packed_size").orElse(DEFAULT_PACKED_SIZE).forGetter(CableDataComponent::packed_size)
            ).apply(instance, CableDataComponent::new)
    );

    public static final StreamCodec<ByteBuf, CableDataComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, CableDataComponent::color,
            ByteBufCodecs.BYTE, CableDataComponent::type,
            ByteBufCodecs.SHORT, CableDataComponent::packed_size,
            CableDataComponent::new
    );

    public static short PACK_SIZE(double width, double height){
        if (! (width <= 0.5-(height/2.0)))//check for collision in the model.
            throw new IllegalArgumentException("Invalid size!, max width(%g)height(%g) or width(%g)height(%g)"
                    .formatted(width,-width*2 +1,width - 0.5 + (height/2.0), height));
        int _x = (int) Math.floor(width*255 + 0.5);
        int _y = (int) Math.floor(height*255 + 0.5);
        return (short) (_x | (_y << 8));
    }

    public static float[] UNPACK_SIZE(short packedSize) {
        return new float[]{(packedSize & 0xff) / 255f, ((packedSize>>8) & 0xff)/255f};
    }

    @Override
    public @Nullable Codec<CableDataComponent> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, CableDataComponent> streamCodec() {
        return STREAM_CODEC;
    }
}
