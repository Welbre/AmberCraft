package welbre.ambercraft.blocks.electrical;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.electrical.ElectricalBE;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModuleFactory;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.electrical.ElectricalElementModule;
import welbre.ambercraft.module.network.NetworkModule;

public class ElectricalBlock extends Block implements EntityBlock {
    public ModuleFactory<ElectricalElementModule, ElectricalBE> factory = new ModuleFactory<>(
            ElectricalBE.class,
            AmberCraft.ModuleTypes.ELECTRICAL_MODULE_TYPE,
            ElectricalElementModule::alloc,
            ElectricalElementModule::free,
            ElectricalBE::setElectricalModule,
            ElectricalBE::getElectricalModule
    );

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElectricalBE(pos,state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return ModulesHolder::TICK_HELPER;
    }

    public ElectricalBlock(Properties p_49795_) {
        super(p_49795_);
    }


    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        factory.create(level, pos);
        if (level.getBlockEntity(pos) instanceof ElectricalBE be)
        {
            for (Direction dir : Direction.values())
            {
                @NotNull Module[] modules = be.getModule(dir);
                if (modules.length > 0 && level.getBlockEntity(pos.relative(dir)) instanceof ModulesHolder holder)
                {
                    for (Module module : modules)
                    {
                       if (module instanceof NetworkModule networkModule)
                           for (Module other : holder.getModule(dir.getOpposite()))
                               if (other instanceof NetworkModule otherNetworkModule)
                                    otherNetworkModule.connect(networkModule);
                    }
                }
            }
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        factory.destroy(level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
