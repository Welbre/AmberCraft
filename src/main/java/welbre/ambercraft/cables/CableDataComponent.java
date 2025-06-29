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

/**
 * @param cable_type_index Internal index to retriever the CableType instance.
 * @param color Represents the cable color(RGB).
 * @param type The type used to check different cable connection.
 * @param packed_size Uses an integer to represent the width and height size of the cable model.
 */
public record CableDataComponent(byte cable_type_index, int color, byte type, short packed_size, boolean ignore_color) implements DataComponentType<CableDataComponent> {
    public static final short DEFAULT_PACKED_SIZE = PACK_SIZE(.4,.4/2);

    public static final Codec<CableDataComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.BYTE.fieldOf("index").forGetter(CableDataComponent::cable_type_index),
                    Codec.INT.fieldOf("color").forGetter(CableDataComponent::color),
                    Codec.BYTE.fieldOf("type").forGetter(CableDataComponent::type),
                    Codec.SHORT.fieldOf("pkd_size").orElse(DEFAULT_PACKED_SIZE).forGetter(CableDataComponent::packed_size),
                    Codec.BOOL.fieldOf("ign_c").orElse(false).forGetter(CableDataComponent::ignore_color)
            ).apply(instance, CableDataComponent::new)
    );

    public static final StreamCodec<ByteBuf, CableDataComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, CableDataComponent::cable_type_index,
            ByteBufCodecs.INT, CableDataComponent::color,
            ByteBufCodecs.BYTE, CableDataComponent::type,
            ByteBufCodecs.SHORT, CableDataComponent::packed_size,
            ByteBufCodecs.BOOL, CableDataComponent::ignore_color,
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
    public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, CableDataComponent> streamCodec() {
        return STREAM_CODEC;
    }

    public CableType getType(){
        return CableType.FromCableTypeIndex(cable_type_index);
    }

    public static final class Builder {
        private byte cable_type_index;
        private int color;
        private byte type;
        private short packed_size;
        private boolean ignore_color;

        public static Builder builder() {
            return new Builder();
        }

        public static Builder builder(CableDataComponent data) {
            var builder = new Builder();
            builder.cable_type_index = data.cable_type_index;
            builder.color = data.color;
            builder.type = data.type;
            builder.packed_size = data.packed_size;
            return builder;
        }

        public Builder setCable_type_index(byte cable_type_index) {
            this.cable_type_index = cable_type_index;
            return this;
        }

        public Builder setColor(int color) {
            this.color = color;
            return this;
        }

        public Builder setType(byte type) {
            this.type = type;
            return this;
        }

        public Builder setPackedSize(short packed_size) {
            this.packed_size = packed_size;
            return this;
        }

        public Builder setIgnoreColor(boolean ignore_color) {
            this.ignore_color = ignore_color;
            return this;
        }

        public CableDataComponent build(){
            return new CableDataComponent(cable_type_index, color, type, packed_size, ignore_color);
        }

        public Builder toggleIgnoreColor() {
            ignore_color = !ignore_color;
            return this;
        }
    }
}
