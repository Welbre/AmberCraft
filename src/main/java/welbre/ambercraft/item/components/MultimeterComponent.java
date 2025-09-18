package welbre.ambercraft.item.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record MultimeterComponent(UUID id, Mode mode) {

    public enum Mode {
        Voltage,
        Current,
        Power,
        Resistence;

        public Mode next()
        {
            if (ordinal() == values().length - 1)
                return Voltage;
            return values()[ordinal() + 1];
        }
    }

    private MultimeterComponent(String string, int mode) {
        this(UUID.fromString(string), Mode.values()[mode]);
    }

    public MultimeterComponent() {
        this(UUID.randomUUID(), Mode.Voltage);
    }

    private String asString() {
        return id.toString();
    }

    private int modeAsInt() {
        return mode.ordinal();
    }

    public static final Codec<MultimeterComponent> CODEC = RecordCodecBuilder.create(uuidInstance ->
            uuidInstance.group(
                    Codec.STRING.fieldOf("id").forGetter(MultimeterComponent::asString),
                    Codec.INT.fieldOf("mode").forGetter(MultimeterComponent::modeAsInt)
            ).apply(uuidInstance,MultimeterComponent::new)
    );

    public static final StreamCodec<ByteBuf, MultimeterComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, MultimeterComponent::asString,
            ByteBufCodecs.INT, MultimeterComponent::modeAsInt,
            MultimeterComponent::new
    );
}
