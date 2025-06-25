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
import welbre.ambercraft.cables.CableStatus;
import welbre.ambercraft.cables.FaceStatus;

import static welbre.ambercraft.cables.CableStatus.GET_FACE_DIRECTIONS;

/**
 * The connection mask is used to represent the internal state of the cable. Any cube face is presented using 5 bits, one for each part of the model.<br>
 * <b>UP(0), LEFT(1), DOWN(2), RIGHT(3), CENTER(4)</b>, so using a combination of this, all 17 states can be presented using 5 bits, for exemple the int 12 or 0b1100
 * mens that the DOWN and RIGHT sides is connected. Because of UP is off so 1*0, LEFT is off so 2*0, DOWN is on so 4 * 1 and RIGHT is on 8 * 1, resulting in 4 + 8 = 12.<br>
 * The center should be rendered if any bit is up, so 0x10001 produce the same model that 0x00001, the CENTER(4) bit is used only in a special case, when a cable is created and don't connect to anything.
 * So the center needs to be rendered, but the other parts don't.<br><br>
 * In total, the FacedCable uses 30 bits. 5 bits per face, and is all stored in the {@link FacedCableBlockEntity#status status} field in the following order.
 * <b>DOWN(0), UP(5), NORTH(10), SOUTH(15), WEST(20), EAST(25)</b>, the number in parentheses mens where the data start of each face.
 */
public class FacedCableBlockEntity extends BlockEntity {
    public static final ModelProperty<CableStatus> CONNECTION_MASK_PROPERTY = new ModelProperty<>();
    private CableStatus status = new CableStatus();

    public FacedCableBlockEntity(BlockPos pos, BlockState blockState) {
        super(Main.Tiles.FACED_CABLE_BLOCK_ENTITY.get(), pos, blockState);
    }

    public CableStatus getStatus(){
        return status;
    }

    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.of(CONNECTION_MASK_PROPERTY, status);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        status = CableStatus.fromRawData(tag.getLongArray("status"));
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        requestModelDataUpdate();
        return tag;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLongArray("status", status.toRawData());
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
        //for each block face
        for (Direction face : status.getCenterDirections())
        {
            BlockPos anchor = pos.relative(face);
            FaceStatus faceStatus = status.getFaceStatus(face);
            //check a possible connection in this face
            for (Direction dir : GET_FACE_DIRECTIONS(face))
            {

                {
                    //check internal connections
                    FaceStatus status = this.status.getFaceStatus(dir);
                    if (status != null && status.color == faceStatus.color && status.type == faceStatus.type)
                    {
                        CONNECT(this, face, dir, this, dir, face, FaceStatus.Connection.INTERNAL);
                        break;
                    }
                }

                //check for blocks on the same plane
                if (level.getBlockEntity(pos.relative(dir)) instanceof FacedCableBlockEntity faced)
                {

                    FaceStatus status = faced.status.getFaceStatus(face);
                    if (status != null && status.color == faceStatus.color && status.type == faceStatus.type)
                        CONNECT(this, face, dir, faced, face, dir.getOpposite(), FaceStatus.Connection.EXTERNAl);
                }
                //check for block on diagonals
                BlockPos diagonal = pos.relative(dir).relative(face);
                if (level.getBlockEntity(diagonal) instanceof FacedCableBlockEntity faced)//diagonal connections
                {
                    BlockPos dia_face_vec = anchor.subtract(diagonal);
                    Direction dia_face = Direction.getApproximateNearest(dia_face_vec.getX(), dia_face_vec.getY(), dia_face_vec.getZ());

                    FaceStatus status = faced.status.getFaceStatus(dia_face);
                    if (status != null && status.color == faceStatus.color && status.type == faceStatus.type)
                        CONNECT(this, face, dir, faced, dia_face, face.getOpposite(), FaceStatus.Connection.DIAGONAL);
                }
            }
        }
    }

    private static void CONNECT(
            FacedCableBlockEntity self, Direction self_face, Direction self_dir,
            FacedCableBlockEntity faced, Direction faced_face, Direction faced_dir, FaceStatus.Connection connection)
    {
        faced.status.rawConnectionSet(faced_face, faced_dir, connection);
        faced.requestModelDataUpdate();
        faced.setChanged();

        self.status.rawConnectionSet(self_face, self_dir, connection);
        self.requestModelDataUpdate();
        self.setChanged();
    }

    public void removeCable(@NotNull LevelReader level, BlockPos pos){//todo instead force all others the recalculate all, just create a function to remove a specified connection
        //for each block face
        for (Direction face : status.getCenterDirections())
        {
            BlockPos anchor = pos.relative(face);
            //check a possible connection in this face
            for (Direction dir : GET_FACE_DIRECTIONS(face))
            {
                if (level.getBlockEntity(pos.relative(dir)) instanceof FacedCableBlockEntity faced)//find a connectable
                {
                    // check if the center of faced contains a cable in the face
                    if (faced.status.getFaceStatus(face) != null)
                    {
                        faced.status.rawConnectionSet(face, dir.getOpposite(), FaceStatus.Connection.EMPTY);
                        faced.requestModelDataUpdate();
                        faced.setChanged();
                    }
                }
                BlockPos diagonal = pos.relative(dir).relative(face);
                if (level.getBlockEntity(diagonal) instanceof FacedCableBlockEntity faced)//diagonal connections
                {
                    BlockPos dia_face_vec = anchor.subtract(diagonal);
                    Direction dia_face = Direction.getApproximateNearest(dia_face_vec.getX(), dia_face_vec.getY(), dia_face_vec.getZ());
                    if (faced.status.getFaceStatus(dia_face) != null)// check if the center of faced contains a cable in the face
                    {
                        faced.status.rawConnectionSet(dia_face, face.getOpposite(), FaceStatus.Connection.EMPTY);
                        faced.requestModelDataUpdate();
                        faced.setChanged();
                    }
                }
            }
        }
    }
}
