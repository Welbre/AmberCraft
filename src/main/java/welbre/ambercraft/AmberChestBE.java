package welbre.ambercraft;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class AmberChestBE extends BlockEntity
{
    public @Nullable ItemStackHandler stackHandle = new ItemStackHandler();

    public AmberChestBE(BlockPos pos, BlockState blockState) {
        super(AmberCraft.BlockEntity.AMBER_BE.get(), pos, blockState);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        stackHandle.deserializeNBT(registries, tag.getCompound("stackHandle"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag nbt = stackHandle.serializeNBT(registries);
        tag.put("stackHandle", nbt);
    }
}
