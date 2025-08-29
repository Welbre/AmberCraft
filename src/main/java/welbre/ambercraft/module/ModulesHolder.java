package welbre.ambercraft.module;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to represent an object that can contains modules.<br>
 * At the moment, only BlockEntity is compatible with this,
 * and maybe this class will be reimplemented as a {@link net.neoforged.neoforge.capabilities.Capabilities}
 * <p>
 *     This interface contains only 3 methods that are very similar to each other.<br>
 *     {@link ModulesHolder#getModules()} is used to read/write data in the holder, and other internal operations like diagnostic tools and debugging.<br>
 *     {@link ModulesHolder#getModule(Direction direction)} is used to handle the connections, this method should return all modules that can be connected at <code>direction</code>.<br>
 *     {@link ModulesHolder#getModule(Object obj)} should be used in yours ModulesHolder implementation instead of {@link ModulesHolder#getModule(Direction)},
 *     the <code>obj</code> can be cast to a direction or any type you want.<br>Exemple: <pre>
Module[] getModule(Object object){
    if (object instanceof Direction dir){
        return getModule(dir);
    }
    else if(...any other type check){

    } else {
        return new Module[0]; //return a empty array, don't return null!
    }
}
 *     </pre>
 * </p>
 */
public abstract class ModulesHolder extends BlockEntity {

    public ModulesHolder(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    /**
     *  Returns all modules that this instance holds.<br>The order of this array is <code color="orange">extremely</code> important, because is used save/loading/synchronization!<br>
     *  The order must be immutable; If you want to add a new Module in your block entity, add in the array <code color="orange">end</code>!
     *  <b>Don't follow this instruction can cause world corruption!</b>
     */
    public abstract @NotNull Module[] getModules();

    /// Returns all modules in <code>direction</code> face.
    public abstract @NotNull Module[] getModule(Direction direction);
    /// Similar to {@link ModulesHolder#getModule(Direction) but used a generic object as extra data.}
    public abstract @NotNull Module[] getModule(Object object);

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        CompoundTag modules = tag.getCompound("modules");
        var mods = getModules();
        for (String localID : modules.getAllKeys())
            mods[Integer.parseInt(localID)].readData(modules.getCompound(localID), registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        CompoundTag modules_tag = new CompoundTag();
        Module[] modules = getModules();

        for (int i = 0; i < modules.length; i++)
        {
            Module module = modules[i];
            var _tag = new CompoundTag();
            module.writeData(_tag, registries);
            modules_tag.put(Integer.toString(i), _tag);
        }
        tag.put("modules", modules_tag);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        var tag = super.getUpdateTag(registries);

        CompoundTag modules_tag = new CompoundTag();
        Module[] modules = getModules();

        for (int i = 0; i < modules.length; i++)
        {
            Module module = modules[i];
            var _tag = new CompoundTag();
            module.writeUpdateTag(_tag, registries);
            modules_tag.put(Integer.toString(i), _tag);
        }
        tag.put("modules", modules_tag);

        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag compoundTag, HolderLookup.Provider lookupProvider) {
        var tag = compoundTag.getCompound("modules");
        var mods = getModules();

        for (String loadID : tag.getAllKeys())
            mods[Integer.parseInt(loadID)].handleUpdateTag(tag.getCompound(loadID), lookupProvider);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
        CompoundTag tag = pkt.getTag();
        CompoundTag main = tag.getCompound("modules");
        var mods = getModules();

        for (String loadID : main.getAllKeys())
            mods[Integer.parseInt(loadID)].onDataPacket(net, main.getCompound(loadID), lookupProvider);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        Module module = null;
        try
        {
            Module[] modules = getModules();
            for (int i = 0; i < modules.length; i++)
            {
                module = modules[i];
                module.onLoad(this);
            }
        }catch (Exception e)
        {
            AmberCraft.LOGGER.error("Error loading heat module for block entity at {} with ID {}", getBlockPos(), module.getID(), e);
            level.removeBlock(getBlockPos(), false);
        }
    }

    public void tick(Level level, BlockPos pos, BlockState state)
    {
        for (Module module : this.getModules())
            module.tick(this);
    }

    /// Use on your EntityBlock passing as a method reference;
    public static <T extends BlockEntity> void TICK_HELPER(Level level, BlockPos pos, BlockState state, T blockEntity)
    {
        if (blockEntity instanceof ModulesHolder holder)
            holder.tick(level, pos, state);
        else
            AmberCraft.LOGGER.error("BlockEntity at {} is not a ModulesHolder!", pos);
    }

    public  <T extends Module> @NotNull T[] getModule(Class<T> aclass, Direction direction){
        Module[] modules = direction == null ? getModules() : getModule(direction);
        List<T> moduleList = new ArrayList<>();
        for (Module module : modules) {
            if (aclass.isInstance(module))
                moduleList.add((T) module);
        }
        return moduleList.toArray((T[]) Array.newInstance(aclass,0));
    }
}
