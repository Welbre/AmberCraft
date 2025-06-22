package welbre.ambercraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.Main;

import java.util.ArrayList;
import java.util.List;

/**
 * The connection mask is used to represent the internal state of the cable. Any cube face is presented using 5 bits, one for each part of the model.<br>
 * <b>UP(0), LEFT(1), DOWN(2), RIGHT(3), CENTER(4)</b>, so using a combination of this, all 17 states can be presented using 5 bits, for exemple the int 12 or 0b1100
 * mens that the DOWN and RIGHT sides is connected. Because of UP is off so 1*0, LEFT is off so 2*0, DOWN is on so 4 * 1 and RIGHT is on 8 * 1, resulting in 4 + 8 = 12.<br>
 * The center should be rendered if any bit is up, so 0x10001 produce the same model that 0x00001, the CENTER(4) bit is used only in a special case, when a cable is created and don't connect to anything.
 * So the center needs to be rendered, but the other parts don't.<br><br>
 * In total, the FacedCable uses 30 bits. 5 bits per face, and is all stored in the {@link FacedCableBlockEntity#connection_mask connection_mask} field in the following order.
 * <b>DOWN(0), UP(5), NORTH(10), SOUTH(15), WEST(20), EAST(25)</b>, the number in parentheses mens where the data start of each face.
 */
public class FacedCableBlockEntity extends BlockEntity {
    public static final ModelProperty<Integer> CONNECTION_MASK_PROPERTY = new ModelProperty<>();
    private int connection_mask = 0;

    public FacedCableBlockEntity(BlockPos pos, BlockState blockState) {
        super(Main.Tiles.FACED_CABLE_BLOCK_ENTITY.get(), pos, blockState);
    }

    public int getConnection_mask() {
        return connection_mask;
    }

    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.of(CONNECTION_MASK_PROPERTY, connection_mask);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        connection_mask = tag.getInt("mask");

    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        //requestModelDataUpdate();
        return tag;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("mask", connection_mask);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
    }

    public void calculateState(@NotNull LevelReader level, BlockPos pos){
        //extract the centers from the mask, so don't lose the cable information, and can rewrite the connections.
        int centers = connection_mask & 0b10000_10000_10000_10000_10000_10000;

        //for each block face
        for (Direction face : getSearchDirection(centers))
        {
            BlockPos anchor = pos.relative(face);
            //check a possible connection in this face
            for (Direction dir : getFaceDirections(face))
            {
                //check internal connections
                if ((centers & (1 << getShiftInFace(dir)+4)) != 0){
                    centers = rawConnectionSet(centers, face, dir, true);
                    centers = rawConnectionSet(centers, dir, face, true);
                }
                else
                {
                    //check for blocks on the same plane
                    if (level.getBlockEntity(pos.relative(dir)) instanceof FacedCableBlockEntity faced)
                    {
                        if ((faced.connection_mask & (1 << getShiftInFace(face) + 4)) != 0)// check if the center of faced contains a cable in the face
                        {
                            faced.rawConnectionSet(face, dir.getOpposite(), true);
                            faced.requestModelDataUpdate();
                            faced.setChanged();

                            centers = rawConnectionSet(centers, face, dir, true);
                        }
                    }
                    //check for block on diagonals
                    BlockPos diagonal = pos.relative(dir).relative(face);
                    if (level.getBlockEntity(diagonal) instanceof FacedCableBlockEntity faced)//diagonal connections
                    {
                        BlockPos dia_face_vec = anchor.subtract(diagonal);
                        Direction dia_face = Direction.getApproximateNearest(dia_face_vec.getX(), dia_face_vec.getY(), dia_face_vec.getZ());
                        if ((faced.connection_mask & (1 << getShiftInFace(dia_face) + 4)) != 0)// check if the center of faced contains a cable in the face
                        {
                            faced.rawConnectionSet(dia_face, face.getOpposite(), true);
                            faced.requestModelDataUpdate();
                            faced.setChanged();

                            centers = rawConnectionSet(centers, face, dir, true);
                        }
                    }
                }
            }
        }
        //check if the connections change, to avoid model requests.
        if (connection_mask != centers)
        {
            connection_mask = centers;
            requestModelDataUpdate();
            setChanged();
        }
    }

    public void removeCable(@NotNull LevelReader level, BlockPos pos){//todo instead force all others the recalculate all, just create a function to remove a specified connection
        //extract the centers from the mask, so don't lose the cable information, and can rewrite the connections.
        int centers = connection_mask & 0b10000_10000_10000_10000_10000_10000;

        //for each block face
        for (Direction face : getSearchDirection(centers))
        {
            BlockPos anchor = pos.relative(face);
            //check a possible connection in this face
            for (Direction dir : getFaceDirections(face))
            {
                if (level.getBlockEntity(pos.relative(dir)) instanceof FacedCableBlockEntity faced)//find a connectable
                {
                    if ((faced.connection_mask & (1 << getShiftInFace(face) + 4)) != 0)// check if the center of faced contains a cable in the face
                    {
                        faced.rawConnectionSet(face, dir.getOpposite(), false);
                        faced.requestModelDataUpdate();
                        faced.setChanged();
                    }
                }
                BlockPos diagonal = pos.relative(dir).relative(face);
                if (level.getBlockEntity(diagonal) instanceof FacedCableBlockEntity faced)//diagonal connections
                {
                    BlockPos dia_face_vec = anchor.subtract(diagonal);
                    Direction dia_face = Direction.getApproximateNearest(dia_face_vec.getX(), dia_face_vec.getY(), dia_face_vec.getZ());
                    if ((faced.connection_mask & (1 << getShiftInFace(dia_face) + 4)) != 0)// check if the center of faced contains a cable in the face
                    {
                        faced.rawConnectionSet(dia_face, face.getOpposite(), false);
                        faced.requestModelDataUpdate();
                        faced.setChanged();
                    }
                }
            }
        }
    }

    /**
     * Put the center cable in the face parameter.<br>
     * Sets the bit CENTER(5) in corresponded face bits in connection_mask.
     * @param face FacedCable face
     */
    public void addCenter(Direction face){
        //the face-first bit address, plus 4 to set the CENTER bit
        int shift = getShiftInFace(face) + 4;
        int preview = connection_mask;
        connection_mask |= (1 << shift);

        if (preview != connection_mask)
        {
            requestModelDataUpdate();
            setChanged();
        }
    }

    /**
     * Raw sets a direction bit in the corresponded face in the connection_mask.<br>
     * This method doesn't check the connection conditions.
     * @param globalDir uses the default directions documented in {@link FacedCableBlockEntity FacedCableBlockEntity}.
     */
    protected void rawConnectionSet(Direction face, Direction globalDir, boolean value){
        connection_mask = rawConnectionSet(connection_mask, face, globalDir, value);
    }

    public static int rawConnectionSet(int mask, Direction face, Direction globalDir, boolean value){
        int shift = getFaceShiftUsingGlobalDirection(face, globalDir);
        if (shift == -1)
            throw new IllegalStateException("Try to get a invalid globalDirection(%s) in face(%s)".formatted(globalDir, face));
        shift += getShiftInFace(face);
        if (value)
            return mask | (1 << shift);
        else
            return mask & ( ~(1<<shift));
    }

    /**
     * Gets the face shift using the block face and a global direction.<br>
     * @return returns the amount of shift in the face mask to modify a specified faceDirection bit, or -1 if it can't get a direction.
     */
    private static int getFaceShiftUsingGlobalDirection(Direction face, Direction globalDir){
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

    public static Direction[] getFaceDirections(Direction dir){
        switch (dir)
        {
            case Direction.UP, Direction.DOWN -> {return new Direction[]{Direction.NORTH, Direction.WEST,Direction.SOUTH,Direction.EAST};}
            case Direction.NORTH, Direction.SOUTH -> {return new Direction[]{Direction.UP, Direction.WEST,Direction.DOWN,Direction.EAST};}
            case Direction.WEST,Direction.EAST -> {return new Direction[]{Direction.NORTH, Direction.UP,Direction.SOUTH,Direction.DOWN};}
            case null -> {return new Direction[0];}
        }
    }

    private static List<Direction> getSearchDirection(int centers){
        List<Direction> dir = new ArrayList<>();
        if ((centers & (1 << 4)) != 0)
            dir.add(Direction.DOWN);
        if ((centers & (1 << 4+5)) != 0)
            dir.add(Direction.UP);
        if ((centers & (1 << 4+10)) != 0)
            dir.add(Direction.NORTH);
        if ((centers & (1 << 4+15)) != 0)
            dir.add(Direction.SOUTH);
        if ((centers & (1 << 4+20)) != 0)
            dir.add(Direction.WEST);
        if ((centers & (1 << 4+25)) != 0)
            dir.add(Direction.EAST);
        return dir;
    }

    private static int getShiftInFace(Direction face){
        return face.get3DDataValue() * 5;
    }

    public boolean hasCableAt(Direction face) {
        return (connection_mask & (1 << getShiftInFace(face) + 4)) != 0;
    }

    public void setMask(int mask) {
        connection_mask = mask;
    }
}
