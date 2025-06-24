package welbre.ambercraft.cables;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class FaceStatus {
    public static final Codec<FaceStatus> CODEC = RecordCodecBuilder.create(data -> data.group(
            Codec.LONG.fieldOf("data").forGetter(FaceStatus::getRawData)
    ).apply(data, FaceStatus::fromRawData));

    /// Represents the connection arrangement in the face.
    public Connection[] connection;
    /// Represents the cable color(RGB).
    public int color = 0;
    /// Represents the cable type, the code checks if is the same type to create a connection.
    public byte type = 0;

    public FaceStatus(Connection[] connection, int color, int type) {
        this.connection = connection;
        this.color = color;
        this.type = (byte) type;
    }

    public FaceStatus(int color, int type) {
        this.color = color;
        this.type = (byte) type;
        this.connection = new Connection[]{Connection.EMPTY, Connection.EMPTY, Connection.EMPTY, Connection.EMPTY};
    }

    public FaceStatus() {
        this(0, 0);
    }

    public long getRawData() {
        long data = 0;
        int shift = 0;
        for (Connection c : connection)
        {
            data |= (long) c.bit << shift;
            shift += 2;
        }
        data |= ((long) color << 2*4);
        data |= ((long) type << 2*4 + 24);
        return data;
    }

    public static FaceStatus fromRawData(long data) {
        Connection[] connections = new Connection[4];
        for (int i = 0; i < 4; i++)
        {
            connections[i] = Connection.values()[(int) ((data >> (i * 2)) & 0b11)];
        }
        int color = (int) (data >> 2*4) & 0xffffff;
        byte type = (byte) ((data >> 24+2*4) & 0xff);
        return new FaceStatus(connections, color, type);
    }

    public FaceStatus connectTo(int dir, Connection connection) {
        this.connection[dir] = connection;
        return this;
    }

    @Override
    public String toString() {
        long data = getRawData();
        return "Connection:0x%x color:0x%x type:%d ".formatted(data & 0b111111111111,color,type);
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
