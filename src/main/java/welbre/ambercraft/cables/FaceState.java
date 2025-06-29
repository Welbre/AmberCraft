package welbre.ambercraft.cables;

import org.jetbrains.annotations.NotNull;

public class FaceState {
    /// Represents the connection arrangement in the face.
    public Connection[] connection;
    public CableDataComponent data;

    public FaceState(Connection[] connection, CableDataComponent data) {
        this.connection = connection;
        this.data = data;
    }

    public FaceState(CableDataComponent data) {
        this(new Connection[]{Connection.EMPTY, Connection.EMPTY, Connection.EMPTY, Connection.EMPTY}, data);
    }

    @Override
    public String toString() {
        return "Connection:0x%x color:0x%x type:%d ignoreColor:%s".formatted(connectionAsByte() & 0b111111,data.color(),data.type(), data.ignore_color() ? "true" : "false");
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
        data |= ((this.data.cable_type_index() & 0xffL) << 8);//1 byte
        data |= ((this.data.color() & 0xffffffL) << 8+8);//3 byte
        data |= (this.data.ignore_color() ? 1L : 0L) << 8+8+24+1;
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
        CableType type = CableType.FromCableTypeIndex((byte) ((data >> 8) & 0xFF));
        return new FaceState(connections, type.getData((int) ((data >> 8+8) & 0xffffff), (data & (1L<<41)) != 0));
    }

    public boolean canConnect(@NotNull FaceState other){
        if (other.data.type() == data.type())
            if (other.data.ignore_color() && data.ignore_color())
                return true;
            else
                return other.data.color() == data.color();
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
