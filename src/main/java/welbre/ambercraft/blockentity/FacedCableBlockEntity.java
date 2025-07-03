package welbre.ambercraft.blockentity;

import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
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
import welbre.ambercraft.cables.*;
import welbre.ambercraft.module.HeatModule;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModulesHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static welbre.ambercraft.cables.CableState.GET_FACE_DIRECTIONS;

/**
 * The connection mask is used to represent the internal state of the cable. Any cube face is presented using 5 bits, one for each part of the model.<br>
 * <b>UP(0), LEFT(1), DOWN(2), RIGHT(3), CENTER(4)</b>, so using a combination of this, all 17 states can be presented using 5 bits, for exemple the int 12 or 0b1100
 * mens that the DOWN and RIGHT sides is connected. Because of UP is off so 1*0, LEFT is off so 2*0, DOWN is on so 4 * 1 and RIGHT is on 8 * 1, resulting in 4 + 8 = 12.<br>
 * The center should be rendered if any bit is up, so 0x10001 produce the same model that 0x00001, the CENTER(4) bit is used only in a special case, when a cable is created and don't connect to anything.
 * So the center needs to be rendered, but the other parts don't.<br><br>
 * In total, the FacedCable uses 30 bits. 5 bits per face, and is all stored in the {@link FacedCableBlockEntity#state status} field in the following order.
 * <b>DOWN(0), UP(5), NORTH(10), SOUTH(15), WEST(20), EAST(25)</b>, the number in parentheses mens where the data start of each face.
 */
public class FacedCableBlockEntity extends BlockEntity implements ModulesHolder {
    public static final ModelProperty<CableState> CONNECTION_MASK_PROPERTY = new ModelProperty<>();
    private CableState state = new CableState();
    private CableBrain brain = new CableBrain();

    public FacedCableBlockEntity(BlockPos pos, BlockState blockState) {
        super(Main.Tiles.FACED_CABLE_BLOCK_ENTITY.get(), pos, blockState);
    }

    public CableState getState(){
        return state;
    }

    public CableBrain getBrain() {
        return brain;
    }

    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.of(CONNECTION_MASK_PROPERTY, state);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        state = CableState.fromRawData(tag.getLongArray("status"));
        //brain = CableBrain.CODEC.parse(NbtOps.INSTANCE, tag.getCompound("brain")).getOrThrow();
        //todo implement this when the networks is done.
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
        tag.putLongArray("status", state.toRawData());
        //tag.put("brain",CableBrain.CODEC.encodeStart(NbtOps.INSTANCE, brain).getOrThrow());
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void calculateState(@NotNull LevelReader level, BlockPos pos){
        //for each block face
        for (Direction face : state.getCenterDirections())
        {
            BlockPos anchor = pos.relative(face);
            FaceState faceState = state.getFaceStatus(face);
            //check a possible connection in this face
            for (Direction dir : GET_FACE_DIRECTIONS(face))
            {

                {
                    //check internal connections
                    var face_status = state.getFaceStatus(dir);
                    if (face_status != null)
                        if (faceState.canConnect(face_status))
                        {
                            CONNECT(this, face, dir, this, dir, face, FaceState.Connection.INTERNAL);
                            continue;
                        } else
                        {
                            CONNECT(this, face, dir, this, dir, face, FaceState.Connection.EMPTY);
                            continue;
                        }
                }

                BlockPos neighbor = pos.relative(dir);
                //check for blocks on the same plane
                if (level.getBlockEntity(neighbor) instanceof FacedCableBlockEntity faced)
                {
                    var status = faced.state.getFaceStatus(face);
                    if (status != null)
                        if (faceState.canConnect(status))
                            CONNECT(this, face, dir, faced, face, dir.getOpposite(), FaceState.Connection.EXTERNAl);
                        else
                            CONNECT(this, face, dir, faced, face, dir.getOpposite(), FaceState.Connection.EMPTY);
                }
                else if (level.getBlockEntity(neighbor) instanceof ModulesHolder holder)
                {
                    @NotNull HeatModule[] module = holder.getModule(HeatModule.class, dir.getOpposite());
                    if (module.length > 0)
                    {
                        state.rawConnectionSet(face, dir, FaceState.Connection.EXTERNAl);
                        requestModelDataUpdate();
                        setChanged();
                    }
                }
                {
                    BlockState state = level.getBlockState(neighbor);
                    if (state.canOcclude() && state.getBlock() != Main.Blocks.ABSTRACT_FACED_CABLE_BLOCK.get())
                        continue;
                }

                //check for block on diagonals
                BlockPos diagonal = neighbor.relative(face);
                if (level.getBlockEntity(diagonal) instanceof FacedCableBlockEntity faced)//diagonal connections
                {
                    BlockPos dia_face_vec = anchor.subtract(diagonal);
                    Direction dia_face = Direction.getApproximateNearest(dia_face_vec.getX(), dia_face_vec.getY(), dia_face_vec.getZ());

                    var status = faced.state.getFaceStatus(dia_face);
                    if (status != null)
                        if (faceState.canConnect(faced.state.getFaceStatus(dia_face)))
                            CONNECT(this, face, dir, faced, dia_face, face.getOpposite(), FaceState.Connection.DIAGONAL);
                        else
                            CONNECT(this, face, dir, faced, dia_face, face.getOpposite(), FaceState.Connection.EMPTY);
                }
            }
        }
    }

    private static void CONNECT(
            FacedCableBlockEntity self, Direction self_face, Direction self_dir,
            FacedCableBlockEntity faced, Direction faced_face, Direction faced_dir, FaceState.Connection connection)
    {
        faced.state.rawConnectionSet(faced_face, faced_dir, connection);
        faced.requestModelDataUpdate();
        faced.setChanged();

        self.state.rawConnectionSet(self_face, self_dir, connection);
        self.requestModelDataUpdate();
        self.setChanged();
    }

    /**
     * Removes a cable from cable face.
     */
    public void removeCable(@NotNull LevelReader level, BlockPos pos, Direction face){
        //for each block face
        BlockPos anchor = pos.relative(face);
        //check a possible connection in this face
        for (Direction dir : GET_FACE_DIRECTIONS(face))
        {
            {
                //check internal connections
                var faceState = this.state.getFaceStatus(dir);
                if (faceState != null)
                {
                    CONNECT(this, face, dir, this, dir, face, FaceState.Connection.EMPTY);
                    continue;
                }
            }
            if (level.getBlockEntity(pos.relative(dir)) instanceof FacedCableBlockEntity faced)//find a connectable
            {
                // check if the center of faced contains a cable in the face
                if (faced.state.getFaceStatus(face) != null)
                {
                    faced.state.rawConnectionSet(face, dir.getOpposite(), FaceState.Connection.EMPTY);
                    faced.requestModelDataUpdate();
                    faced.setChanged();
                }
            }
            BlockPos diagonal = pos.relative(dir).relative(face);
            if (level.getBlockEntity(diagonal) instanceof FacedCableBlockEntity faced)//diagonal connections
            {
                BlockPos dia_face_vec = anchor.subtract(diagonal);
                Direction dia_face = Direction.getApproximateNearest(dia_face_vec.getX(), dia_face_vec.getY(), dia_face_vec.getZ());
                if (faced.state.getFaceStatus(dia_face) != null)// check if the center of faced contains a cable in the face
                {
                    faced.state.rawConnectionSet(dia_face, face.getOpposite(), FaceState.Connection.EMPTY);
                    faced.requestModelDataUpdate();
                    faced.setChanged();
                }
            }
        }
        removeCenter(face);
        requestModelDataUpdate();
        setChanged();
    }

    public void addCenter(Direction face, AmberFCableComponent component)
    {
        state.addCenter(face, component);
        brain.addCenter(face, component, this);
    }

    public void removeCenter(Direction face)
    {
        state.removeCenter(face);
        brain.removeCenter(face);
    }

    @Override
    public Module[] getModules() {
        List<Module> list = new ArrayList<>();
        for (Direction dir : Direction.values())
        {
            FaceBrain brain = this.brain.getFaceBrain(dir);
            if (brain != null)
                list.addAll(Arrays.stream(brain.getModules()).toList());
        }
        return list.toArray(Module[]::new);
    }

    @Override
    public Module[] getModule(Direction direction) {
        return getModules();
    }
}
