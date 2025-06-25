package welbre.ambercraft.cables;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class FaceStatus {
    public static final Codec<FaceStatus> CODEC = RecordCodecBuilder.create(data -> data.group(
            Codec.LONG.fieldOf("data").forGetter(FaceStatus::toRawData)
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

    public FaceStatus connectTo(int dir, Connection connection) {
        this.connection[dir] = connection;
        return this;
    }

    @Override
    public String toString() {
        return "Connection:0x%x color:0x%x type:%d ".formatted(connectionAsByte() & 0b11111111,color,type);
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
        data |= (connection & 0xff);
        data |= ((color & 0xffffffffL) << 8);
        data |= (type & 0xffL) << 8+32;
        return data;
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

    public static FaceStatus fromRawData(long data) {
        Connection[] connections = new Connection[4];
        int shift = 0;
        for (int i = 0; i < 4; i++)
        {
            connections[i] = Connection.values()[(int) ((data >> shift) & 0b11)];
            shift += 2;
        }
        return new FaceStatus(connections, (int) ((data >> 8) & 0xffffffffL), (byte) ((data >> 8+32) & 0xff));
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
