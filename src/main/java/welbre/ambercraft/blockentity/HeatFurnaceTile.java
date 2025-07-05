package welbre.ambercraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import welbre.ambercraft.Main;
import welbre.ambercraft.module.HeatModule;

public class HeatFurnaceTile extends HeatBlockEntity {
    private int timer = 0;
    private int boost = 1;
    private boolean overcharged = false;

    public HeatFurnaceTile(BlockPos pos, BlockState blockState) {
        super(Main.Tiles.HEAT_FURNACE_TILE.get(), pos, blockState);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null) {
            BlockEntity entity = level.getBlockEntity(getBlockPos());
            if (entity instanceof HeatFurnaceTile tile) {
                tile.setOverCharged(level.getBlockState(getBlockPos().below()).getBlock() == Blocks.LAVA);
            }
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (!level.isClientSide) {
            if (blockEntity instanceof HeatFurnaceTile furnace) {
                if (!furnace.overcharged)
                    return;
                if (furnace.timer++ >= 5) {
                    furnace.heatModule.getHeatNode().transferHeat(furnace.boost);
                    furnace.timer = 0;
                    if (furnace.heatModule.getHeatNode().getTemperature() > 1000)
                    {
                        furnace.heatModule.free();
                        level.setBlock(pos, Blocks.LAVA.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                    level.sendBlockUpdated(pos,state,state, Block.UPDATE_CLIENTS);
                }
                level.sendBlockUpdated(pos,state,state, Block.UPDATE_CLIENTS);
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        boost = tag.getInt("boost");
        overcharged = tag.getBoolean("overcharged");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("boost",boost);
        tag.putBoolean("overcharged",overcharged);
    }

    public void setOverCharged(boolean b) {
        overcharged = b;
    }

    public void addBoost() {
        boost+=10;
    }
}
