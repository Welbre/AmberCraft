package welbre.ambercraft.blockentity.electrical;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.electrical.ElectricalCableModule;

public class InsulatorBE extends ModulesHolder
{
    protected BlockPos[] cablePos = new BlockPos[0];
    protected ElectricalCableModule cableModule = new ElectricalCableModule(0.5);

    public InsulatorBE(BlockPos pos, BlockState blockState) {
        super(AmberCraft.BlockEntity.INSULATOR_BE.get(), pos, blockState);
    }

    @Override
    public @NotNull Module[] getModules() {
        return new Module[]{cableModule};
    }

    public ElectricalCableModule getCableModule() {
        return cableModule;
    }

    public void setCableModule(ElectricalCableModule cableModule) {
        this.cableModule = cableModule;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        if (tag.contains("cable"))
        {
            long[] cable = tag.getLongArray("cable");
            cablePos = new BlockPos[cable.length];

            for (int i = 0; i < cable.length; i++)
                cablePos[i] = BlockPos.of(cable[i]);
        }
        else
            cablePos = new BlockPos[0];
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        if (cablePos.length > 0)
        {
            long[] cable = new long[cablePos.length];

            for (int i = 0; i < cablePos.length; i++)
                cable[i] = cablePos[i].asLong();

            tag.putLongArray("cable", cable);
        }

        super.saveAdditional(tag, registries);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        var tag =super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag compoundTag, HolderLookup.Provider lookupProvider) {
        loadAdditional(compoundTag, lookupProvider);
        super.handleUpdateTag(compoundTag, lookupProvider);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level == null || level.isClientSide)
            return;
        for (BlockPos cable : cablePos)
            if (level.getBlockEntity(cable) instanceof InsulatorBE other)
                other.getCableModule().connect(this.getCableModule());
    }

    public BlockPos[] getCablePos() {
        return cablePos;
    }

    public boolean isConnected()
    {
        return cablePos.length > 0;
    }

    public void addCablePos(BlockPos cablePos)
    {
        //ignores duplicated cables
        if (isConnected())
            for (BlockPos pos : this.cablePos)
                if (pos.equals(cablePos))
                    return;

        var temp = new BlockPos[this.cablePos.length + 1];
        System.arraycopy(this.cablePos, 0, temp, 0, this.cablePos.length);
        temp[this.cablePos.length] = cablePos;

        this.cablePos = temp;
    }

    public void removeCablePos(BlockPos cablePos)
    {
        var temp = new BlockPos[this.cablePos.length - 1];
        boolean found = false;
        for (int i = 0; i < temp.length; i++)
        {
            if (found)
                temp[i-1] = this.cablePos[i];
            else
                if (cablePos.equals(this.cablePos[i]))
                    found = true;
                else
                    temp[i] = this.cablePos[i];
        }

        this.cablePos = temp;
    }

    @Override
    public @NotNull Module[] getModule(Direction direction) {
        if (direction != Direction.UP)
            return getModules();
        else
            return new Module[0];
    }
}
