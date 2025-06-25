package welbre.ambercraft.cables;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import welbre.ambercraft.blockentity.FacedCableBlockEntity;
import welbre.ambercraft.cables.FaceStatus.Connection;

import java.util.ArrayList;
import java.util.List;

public class CableStatus {
    public static final Codec<CableStatus> CODEC = RecordCodecBuilder.create( data -> data.group(
            FaceStatus.CODEC.listOf().fieldOf("faces").forGetter(status -> List.of(status.up, status.down, status.north, status.south, status.west, status.east))
    ).apply(data, CableStatus::new));

    private FaceStatus up= null;
    private FaceStatus down= null;
    private FaceStatus north= null;
    private FaceStatus south= null;
    private FaceStatus west= null;
    private FaceStatus east= null;

    private CableStatus(List<FaceStatus> faces){
        this(faces.getFirst(), faces.get(1), faces.get(2), faces.get(3), faces.get(4), faces.get(5));
    }

    public CableStatus() {
    }

    public CableStatus(FaceStatus up, FaceStatus down, FaceStatus north, FaceStatus south, FaceStatus west, FaceStatus east) {
        this.up = up;
        this.down = down;
        this.north = north;
        this.south = south;
        this.west = west;
        this.east = east;
    }

    /**
     * Get all centers faces.
     */
    public List<Direction> getCenterDirections(){
        List<Direction> dir = new ArrayList<>();
        if (down != null)
            dir.add(Direction.DOWN);
        if (up != null)
            dir.add(Direction.UP);
        if (north != null)
            dir.add(Direction.NORTH);
        if (south != null)
            dir.add(Direction.SOUTH);
        if (west != null)
            dir.add(Direction.WEST);
        if (east != null)
            dir.add(Direction.EAST);
        return dir;
    }

    /**
     * Raw sets a direction bit in the corresponded face in the connection_mask.<br>
     * This method doesn't check the connection conditions.
     * @param globalDir uses the default directions documented in {@link FacedCableBlockEntity FacedCableBlockEntity}.
     */
    public void rawConnectionSet(Direction face, Direction globalDir, Connection connection){
        FaceStatus status = getFaceStatus(face);
        status.connection[getConnectionIndexByGlobalDir(face,globalDir)] = connection;
    }

    /**
     * Put the center cable in the face parameter.<br>
     * Sets the bit CENTER(5) in corresponded face bits in connection_mask.
     * @param face FacedCable face
     */
    public void addCenter(Direction face, int color, int type){
        switch (face){
            case UP -> up = new FaceStatus(color, type);
            case DOWN -> down = new FaceStatus(color, type);
            case NORTH -> north = new FaceStatus(color, type);
            case SOUTH -> south = new FaceStatus(color, type);
            case EAST -> east = new FaceStatus(color, type);
            case WEST -> west = new FaceStatus(color, type);
        }
    }

    public FaceStatus getFaceStatus(Direction face){
        return switch (face){
            case UP -> up;
            case DOWN -> down;
            case NORTH -> north;
            case SOUTH -> south;
            case EAST -> east;
            case WEST -> west;
        };
    }

    public long[] toRawData() {
        return new long[]{up == null ? -1 : up.toRawData(), down == null ? -1 : down.toRawData(),north == null ? -1 : north.toRawData(),south == null ? -1 : south.toRawData(),west == null ? -1 : west.toRawData(),east == null ? -1 : east.toRawData()};
    }

    public static CableStatus fromRawData(long[] data){
        return new CableStatus(
                data[0] == -1 ? null : FaceStatus.fromRawData(data[0]),
                data[1] == -1 ? null : FaceStatus.fromRawData(data[1]),
                data[2] == -1 ? null : FaceStatus.fromRawData(data[2]),
                data[3] == -1 ? null : FaceStatus.fromRawData(data[3]),
                data[4] == -1 ? null : FaceStatus.fromRawData(data[4]),
                data[5] == -1 ? null : FaceStatus.fromRawData(data[5])
        );
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        for (Direction dir : Direction.values())
        {
            FaceStatus status = getFaceStatus(dir);
            if (status != null)
            {
                builder.append("%s: %s".formatted(dir.name().toLowerCase(), status.toString()));
            }
        }
        builder.append("}");
        return builder.toString();
    }

    public static Direction[] GET_FACE_DIRECTIONS(Direction dir){
        switch (dir)
        {
            case Direction.UP, Direction.DOWN -> {return new Direction[]{Direction.NORTH, Direction.WEST,Direction.SOUTH,Direction.EAST};}
            case Direction.NORTH, Direction.SOUTH -> {return new Direction[]{Direction.UP, Direction.WEST,Direction.DOWN,Direction.EAST};}
            case Direction.WEST,Direction.EAST -> {return new Direction[]{Direction.NORTH, Direction.UP,Direction.SOUTH,Direction.DOWN};}
            case null -> {return new Direction[0];}
        }
    }

    /**
     * Gets the face shift using the block face and a global direction.<br>
     * @return returns the amount of shift in the face mask to modify a specified faceDirection bit, or -1 if it can't get a direction.
     */
    private static int getConnectionIndexByGlobalDir(Direction face, Direction globalDir){
        return switch (face)
        {
            case DOWN -> switch (globalDir)
            {
                case NORTH -> 0;
                case WEST -> 1;
                case SOUTH -> 2;
                case EAST -> 3;
                default -> -1;
            };
            case UP -> switch (globalDir)
            {
                case NORTH -> 2;
                case WEST -> 1;
                case SOUTH -> 0;
                case EAST -> 3;
                default -> -1;
            };
            case NORTH -> switch (globalDir)
            {
                case UP -> 0;
                case WEST -> 1;
                case DOWN -> 2;
                case EAST -> 3;
                default -> -1;
            };
            case SOUTH -> switch (globalDir)
            {
                case UP -> 0;
                case EAST -> 1;
                case DOWN -> 2;
                case WEST -> 3;
                default -> -1;
            };
            case WEST -> switch (globalDir)
            {
                case UP -> 0;
                case SOUTH -> 1;
                case DOWN -> 2;
                case NORTH -> 3;
                default -> -1;
            };
            case EAST -> switch (globalDir)
            {
                case UP -> 0;
                case NORTH -> 1;
                case DOWN -> 2;
                case SOUTH -> 3;
                default -> -1;
            };
        };
    }
}
