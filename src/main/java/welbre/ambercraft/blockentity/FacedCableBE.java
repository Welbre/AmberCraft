package welbre.ambercraft.blockentity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.cables.*;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.heat.HeatModule;
import welbre.ambercraft.module.network.NetworkModule;

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
 * In total, the FacedCable uses 30 bits. 5 bits per face, and is all stored in the {@link FacedCableBE#state status} field in the following order.
 * <b>DOWN(0), UP(5), NORTH(10), SOUTH(15), WEST(20), EAST(25)</b>, the number in parentheses mens where the data start of each face.
 */
public class FacedCableBE extends ModulesHolder {
    public static final ModelProperty<CableState> CONNECTION_MASK_PROPERTY = new ModelProperty<>();
    private CableState state = new CableState();
    private CableBrain brain = new CableBrain();

    public FacedCableBE(BlockPos pos, BlockState blockState) {
        super(AmberCraft.BlockEntity.FACED_CABLE_BLOCK_BE.get(), pos, blockState);
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
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLongArray("status", state.toRawData());
        tag.put("brain", CableBrain.CODEC.encodeStart(NbtOps.INSTANCE, brain).getOrThrow());
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        state = CableState.fromRawData(tag.getLongArray("status"));
        brain = CableBrain.CODEC.parse(NbtOps.INSTANCE, tag.getCompound("brain")).getOrThrow();
        super.loadAdditional(tag, registries);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        loadAdditional(tag, lookupProvider);
        super.handleUpdateTag(tag, lookupProvider);
        requestModelDataUpdate();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
        requestModelDataUpdate();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onLoad() {

    }

    public record UpdateShapeResult(boolean changed, BlockPos[] diagonal){}
    public UpdateShapeResult updateState()
    {
        var pos = getBlockPos();
        var level = getLevel();
        List<BlockPos> diagonals = new ArrayList<>(4);
        boolean changed = false;
        assert level != null;

        for (Direction face : state.getCenterDirections())
        {
            final FaceState.Connection[] old = Arrays.copyOf(state.getFaceStatus(face).connection,4);
            BlockPos anchor = pos.relative(face);
            FaceState faceState = state.getFaceStatus(face);

            for (Direction dir : GET_FACE_DIRECTIONS(face))
            {
                {//internal connection
                    FaceState face_status = state.getFaceStatus(dir);
                    if (face_status != null)
                        this.state.rawConnectionSet(face, dir, faceState.canConnect(face_status) ? FaceState.Connection.INTERNAL : FaceState.Connection.EMPTY);
                    else
                        this.state.rawConnectionSet(face, dir, FaceState.Connection.EMPTY);
                }

                //external connection
                BlockPos neighbor = pos.relative(dir);
                if (level.getBlockEntity(neighbor) instanceof ModulesHolder holder)
                {
                    if (holder instanceof FacedCableBE other)
                    {
                        if (other.state.getFaceStatus(face) != null)
                            this.state.rawConnectionSet(face, dir, other.state.getFaceStatus(face).canConnect(faceState) ? FaceState.Connection.EXTERNAl : FaceState.Connection.EMPTY);
                            //changed |= CONNECT(this, face, dir, other, face, dir.getOpposite(), other.state.getFaceStatus(face).canConnect(faceState) ? FaceState.Connection.EXTERNAl : FaceState.Connection.EMPTY);
                    }
                    else
                    {
                        @NotNull NetworkModule[] modules = holder.getModule(NetworkModule.class, dir.getOpposite());
                        this.state.rawConnectionSet(face, dir, modules.length > 0 ? FaceState.Connection.EXTERNAl : FaceState.Connection.EMPTY);
                    }
                }

                //diagonal connection
                BlockPos diagonal = neighbor.relative(face);
                BlockState n_state = level.getBlockState(neighbor);
                if ((n_state.isAir() || n_state.getBlock() == AmberCraft.Blocks.ABSTRACT_FACED_CABLE_BLOCK.get()) && level.getBlockEntity(diagonal) instanceof FacedCableBE other)
                {
                    BlockPos dia_face_vec = anchor.subtract(diagonal);
                    Direction dia_face = Direction.getApproximateNearest(dia_face_vec.getX(), dia_face_vec.getY(), dia_face_vec.getZ());

                    FaceState face_state = other.state.getFaceStatus(dia_face);
                    if (face_state != null)
                    {
                        //changed |= CONNECT(this, face, dir, other, dia_face, face.getOpposite(), faceState.canConnect(face_state) ? FaceState.Connection.DIAGONAL : FaceState.Connection.EMPTY);//todo check if the dir is current
                        this.state.rawConnectionSet(face, dir, faceState.canConnect(face_state) ? FaceState.Connection.DIAGONAL : FaceState.Connection.EMPTY);
                        diagonals.add(diagonal);
                    }
                    else
                        this.state.rawConnectionSet(face, dir, FaceState.Connection.EMPTY);
                }
            }
            if (!Arrays.equals(old, state.getFaceStatus(face).connection))
                changed = true;
        }

        return new UpdateShapeResult(changed, diagonals.toArray(BlockPos[]::new));
    }

    public void updateNeighborhood() {

    }

    public void reRender()
    {
        if (level != null && level instanceof ClientLevel clientLevel)
        {
            var pos = getBlockPos();
            Minecraft.getInstance().levelRenderer.setBlocksDirty(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
            requestModelDataUpdate();
        }
    }

    public void updateBrain() {
        var level = getLevel();
        var pos = getBlockPos();

        if (level == null)
            return;
        if (level.isClientSide())
            return;

        for (Direction face : brain.getCenterDirections())
        {
            BlockPos anchor = pos.relative(face);
            FaceBrain faceBrain = brain.getFaceBrain(face);
            if (faceBrain == null)//should be null
                continue;

            for (Direction dir : GET_FACE_DIRECTIONS(face))
            {
                {//internal connection
                    FaceBrain face_brain = brain.getFaceBrain(dir);
                    if (face_brain != null)
                        faceBrain.connectModules(face_brain.modules());
                }

                //external connection
                BlockPos neighbor = pos.relative(dir);
                if (level.getBlockEntity(neighbor) instanceof ModulesHolder holder)
                {
                    NetworkModule[] modules = null;
                    if (holder instanceof FacedCableBE other)//check if the cables can connect in the case that holder is a FacedCable
                        if (other.state.getFaceStatus(face) != null)
                            if (other.state.getFaceStatus(face).canConnect(state.getFaceStatus(face)))
                                if (other.getBrain().getFaceBrain(face) != null)
                                    modules = Arrays.stream(other.getBrain().getFaceBrain(face).modules()).filter(module -> module instanceof NetworkModule).toArray(NetworkModule[]::new);
                    else
                        modules = holder.getModule(NetworkModule.class, dir.getOpposite());

                    if (modules != null && modules.length > 0)
                        faceBrain.connectModules(modules);
                }

                //diagonal connection
                BlockPos diagonal = neighbor.relative(face);
                BlockState n_state = level.getBlockState(neighbor);
                if ((n_state.isAir() || n_state.getBlock() == AmberCraft.Blocks.ABSTRACT_FACED_CABLE_BLOCK.get()) && level.getBlockEntity(diagonal) instanceof FacedCableBE other)
                {
                    BlockPos dia_face_vec = anchor.subtract(diagonal);
                    Direction dia_face = Direction.getApproximateNearest(dia_face_vec.getX(), dia_face_vec.getY(), dia_face_vec.getZ());

                    FaceBrain face_brain = other.brain.getFaceBrain(dia_face);
                    if (face_brain != null)
                        faceBrain.connectModules(face_brain.modules());
                }
            }
        }
    }

    /**
     * update the cable state, disconnect all modules in the brain, and update the modules after mark-andNotifyBlock to render in the client.
     */
    public void updateAll(Level level, BlockPos pos) {
        updateState();

        if (!level.isClientSide())//server only
        {
            for (Direction face : brain.getCenterDirections())
                for (Module module : brain.getFaceBrain(face).modules())
                    if (module instanceof NetworkModule network)
                        network.disconnectAll();//todo fix, problem with disconnect/remove modules
            updateBrain();
        }
    }

    private static void CONNECT(
            FacedCableBE self, Direction self_face, Direction self_dir,
            FacedCableBE faced, Direction faced_face, Direction faced_dir,
            FaceState.Connection connection, boolean isClientSide
    ) {
        Level level = self.getLevel();
        faced.state.rawConnectionSet(faced_face, faced_dir, connection);
        self.state.rawConnectionSet(self_face, self_dir, connection);
        faced.setChanged();
        self.setChanged();
    }

    /**
     * Removes a cable from cable face.
     */
    public void removeCable(@NotNull LevelReader level, BlockPos pos, Direction face, boolean isClientSide){
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
                    CONNECT(this, face, dir, this, dir, face, FaceState.Connection.EMPTY, isClientSide);
                    continue;
                }
            }
            if (level.getBlockEntity(pos.relative(dir)) instanceof FacedCableBE faced)//find a connectable
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
            if (level.getBlockEntity(diagonal) instanceof FacedCableBE faced)//diagonal connections
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

    public void addCenter(Direction face, FacedCableComponent component)
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
    public @NotNull Module[] getModules() {
        List<Module> list = new ArrayList<>();
        for (Direction dir : Direction.values())
        {
            FaceBrain brain = this.brain.getFaceBrain(dir);
            if (brain != null)
                list.addAll(Arrays.stream(brain.modules()).toList());
        }
        return list.toArray(Module[]::new);
    }

    @Override
    public @NotNull Module[] getModule(Direction direction) {
        return getModules();
    }

    @Override
    public @NotNull Module[] getModule(Object object) {
        if (object instanceof Direction dir)
            return getModule(dir);
        return new Module[0];
    }

    public void setState(CableState state) {
        this.state = state;
    }
}
