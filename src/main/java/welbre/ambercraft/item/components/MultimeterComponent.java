package welbre.ambercraft.item.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record MultimeterComponent(UUID id) {

    private MultimeterComponent(String string) {
        this(UUID.fromString(string));
    }

    private String asString() {
        return id.toString();
    }

    public static final Codec<MultimeterComponent> CODEC = RecordCodecBuilder.create(uuidInstance ->
            uuidInstance.group(
                    Codec.STRING.fieldOf("id").forGetter(MultimeterComponent::asString)
            ).apply(uuidInstance,MultimeterComponent::new)
    );

    public static final StreamCodec<ByteBuf, MultimeterComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, MultimeterComponent::asString,
            MultimeterComponent::new
    );
}
