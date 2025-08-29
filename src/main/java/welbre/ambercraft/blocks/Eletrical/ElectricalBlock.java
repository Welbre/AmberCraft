package welbre.ambercraft.blocks.Eletrical;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.ElectricalBE;
import welbre.ambercraft.module.ModuleFactory;
import welbre.ambercraft.module.electrical.ElectricalModule;

public class ElectricalBlock extends Block {
    public static final ModuleFactory<ElectricalModule, ElectricalBE> factory = new ModuleFactory<>(
            ElectricalBE.class,
            AmberCraft.ModuleTypes.ELECTRICAL_MODULE_TYPE,
            ElectricalModule::alloc,
            ElectricalModule::free,
            ElectricalBE::setModule,
            ElectricalBE::getModule
    );

    public ElectricalBlock(Properties p_49795_) {
        super(p_49795_);
    }


    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        factory.create(level, pos);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        factory.destroy(level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
