package welbre.ambercraft.cables;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class FaceState {
    /// Represents the connection arrangement in the face.
    public Connection[] connection;
    public CableType type;
    public CableData data;

    public FaceState(Connection[] connection, AmberFCableComponent component) {
        this.connection = connection;
        this.type = component.getType();
        this.data = component.get();
    }

    public FaceState(AmberFCableComponent component) {
        this(new Connection[]{Connection.EMPTY, Connection.EMPTY, Connection.EMPTY, Connection.EMPTY}, component);
    }

    public FaceState(FaceState state)
    {
        this.connection = Arrays.copyOf(state.connection, state.connection.length);
        this.type = state.type;
        this.data = new CableData(state.data);
    }

    @Override
    public String toString() {
        return "Connection:0x%x color:0x%x type:%d ignoreColor:%s".formatted(connectionAsByte() & 0b111111,data.color,data.type, data.ignoreColor ? "true" : "false");
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
        data |= ((this.type.cable_type_index & 0xffL) << 8);//1 byte
        data |= ((this.data.color & 0xffffffL) << 8+8);//3 byte
        data |= (this.data.ignoreColor ? 1L : 0L) << 8+8+24+1;
        //2 bytes 7 bits left
        return data;
    }


    public static FaceState fromRawData(long data) {
        Connection[] connections = new Connection[4];
        int shift = 0;
        for (int i = 0; i < 4; i++)
        {
            connections[i] = Connection.values()[(int) ((data >> shift) & 0b11)];
            shift += 2;
        }
        //todo check if is working.
        CableType type = CableType.FromCableTypeIndex((byte) ((data >> 8) & 0xFF));
        return new FaceState(connections, new AmberFCableComponent(type.cable_type_index, new CableData(
                (int) ((data >> 16) & 0xffffff),
                type.getType(),
                ((data >> 8+8+24+1) & 0x1) == 1
        )));
    }

    public boolean canConnect(@NotNull FaceState other){
        if (other.data.type == data.type)
            if (other.data.ignoreColor && data.ignoreColor)
                return true;
            else
                return other.data.color == data.color;
        return false;
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
