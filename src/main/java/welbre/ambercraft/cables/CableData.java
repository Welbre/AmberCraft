package welbre.ambercraft.cables;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class CableData {
    public int color;
    public byte type;
    public boolean ignoreColor;

    public CableData(int color, byte type, boolean ignoreColor) {
        this.color = color;
        this.type = type;
        this.ignoreColor = ignoreColor;
    }

    public CableData(CableData data) {
        this.color = data.color & 0xffffff;//only the rgb
        this.type = data.type;
        this.ignoreColor = data.ignoreColor;
    }

    public int getColor() {
        return color;
    }

    public byte getType() {
        return type;
    }

    public boolean isIgnoreColor() {
        return ignoreColor;
    }

    public static final Codec<CableData> CODEC = RecordCodecBuilder.create(
            ins -> ins.group(
                    Codec.INT.fieldOf("color").forGetter(CableData::getColor),
                    Codec.BYTE.fieldOf("type").forGetter(CableData::getType),
                    Codec.BOOL.fieldOf("ign_c").forGetter(CableData::isIgnoreColor)
            ).apply(ins, CableData::new)
    );

    public static final StreamCodec<ByteBuf,CableData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, CableData::getColor,
            ByteBufCodecs.BYTE, CableData::getType,
            ByteBufCodecs.BOOL, CableData::isIgnoreColor,
            CableData::new
    );

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CableData data)
        {
            if (data.type != type)
                return false;
            if (data.color != color)
                return false;
            return data.ignoreColor == ignoreColor;
        }
        return false;
    }
}
