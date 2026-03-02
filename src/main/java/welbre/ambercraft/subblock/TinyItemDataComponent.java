package welbre.ambercraft.subblock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Holds a reference to a Tiny block in {@link TinyBlockRegister}.<br>
 * Notice that a string is used, and the constructor always check if the string is a valid tiny block.
 * @param tinyBlockKey
 */
public record TinyItemDataComponent(String tinyBlockKey)
{
    public static final Codec<TinyItemDataComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("tinyBlockKey").forGetter(TinyItemDataComponent::tinyBlockKey)
            ).apply(instance, TinyItemDataComponent::new)
    );
    public static final StreamCodec<ByteBuf, TinyItemDataComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, TinyItemDataComponent::tinyBlockKey,
            TinyItemDataComponent::new
    );

    public TinyItemDataComponent(@NotNull ResourceLocation resource)
    {
        this(resource.toString());
    }

    public TinyItemDataComponent(@NotNull TinyBlock tinyBlock)
    {
        this(tinyBlock.registerName);
    }

    public TinyItemDataComponent
    {
        if (TinyBlockRegister.FROM_STRING(tinyBlockKey) == null)
            throw new IllegalArgumentException("Invalid tiny block key (%s) for Tiny item data component".formatted(tinyBlockKey));
    }

    public @NotNull TinyBlock get(){return TinyBlockRegister.FROM_STRING(tinyBlockKey);}
}
