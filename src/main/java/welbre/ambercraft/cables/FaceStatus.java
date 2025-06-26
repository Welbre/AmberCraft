package welbre.ambercraft.cables;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public class FaceStatus {
    public static final Codec<FaceStatus> CODEC = RecordCodecBuilder.create(data -> data.group(
            Codec.LONG.fieldOf("data").forGetter(FaceStatus::toRawData)
    ).apply(data, FaceStatus::fromRawData));

    /// Represents the connection arrangement in the face.
    public Connection[] connection;
    /// Represents the cable color(RGB).
    public int color;
    /// Represents the cable type. The code checks if is the same type to create a connection.<br> -1 is a special case, is used to connect different colors cables.
    public byte type;
    /// Uses an integer to represent the x,y,z size of the cable model.
    public short packed_size = CableDataComponent.DEFAULT_PACKED_SIZE;

    public FaceStatus(Connection[] connection, int color, int type, short packed_size) {
        this.connection = connection;
        this.color = color;
        this.type = (byte) type;
        this.packed_size = packed_size;
    }

    public FaceStatus(int color, int type) {
        this.color = color & 0xffffff;
        this.type = (byte) type;
        this.connection = new Connection[]{Connection.EMPTY, Connection.EMPTY, Connection.EMPTY, Connection.EMPTY};
    }

    public FaceStatus(CableDataComponent component) {
        this(component.color(), component.type());
        this.packed_size = component.packed_size();
    }

    @Override
    public String toString() {
        return "Connection:0x%x color:0x%x type:%d ".formatted(connectionAsByte() & 0b111111,color,type);
    }

    private byte connectionAsByte() {
        byte data = 0;
        int shift = 0;
        for (Connection c : connection)
        {
            data |= (byte) (c.bit << shift);
            shift += 2;
        }
        return data;
    }

    public long toRawData(){
        long data = 0;
        byte connection = connectionAsByte();
        data |= (connection & 0xff);//1 byte
        data |= ((color & 0xffffffL) << 8);//3 byte
        data |= (type & 0xffL) << 8+24;// 1byte
        data |= (packed_size & 0xffffL) << 8+24+8;//2 byte
        //1 byte left
        return data;
    }


    public static FaceStatus fromRawData(long data) {
        Connection[] connections = new Connection[4];
        int shift = 0;
        for (int i = 0; i < 4; i++)
        {
            connections[i] = Connection.values()[(int) ((data >> shift) & 0b11)];
            shift += 2;
        }
        return new FaceStatus(connections, (int) ((data >> 8) & 0xffffffL), (byte) ((data >> 8+24) & 0xff), (short) ((data >> 8+24+8)&0xffff));
    }

    public boolean canConnect(@NotNull FaceStatus other){
        if (other.type == -1 && type == -1)
            return true;
        else {
            if (other.type == -1 || type == -1)
                return other.color == color;
            else
                return other.color == color && other.type == type;
        }
    }

    public enum Connection {
        /// No connection
        EMPTY(0),
        /// Connected to another block in the side.
        EXTERNAl(1),
        /// Connected to diagonal block.
        DIAGONAL(2),
        /// Connected to another center in the same block.
        INTERNAL(3);
        public final int bit;

        Connection(int bit) {
            this.bit = bit;
        }
    }
}
