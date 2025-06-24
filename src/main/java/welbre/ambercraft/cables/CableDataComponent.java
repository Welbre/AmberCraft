package welbre.ambercraft.cables;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

public record CableDataComponent(int color, byte type) implements DataComponentType<CableDataComponent> {

    public static final Codec<CableDataComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("color").forGetter(CableDataComponent::color),
                    Codec.BYTE.fieldOf("type").forGetter(CableDataComponent::type)
            ).apply(instance, CableDataComponent::new)
    );

    public static final StreamCodec<ByteBuf, CableDataComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, CableDataComponent::color,
            ByteBufCodecs.BYTE, CableDataComponent::type,
            CableDataComponent::new
    );

    @Override
    public @Nullable Codec<CableDataComponent> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, CableDataComponent> streamCodec() {
        return STREAM_CODEC;
    }
}
