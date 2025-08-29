package welbre.ambercraft.module.electrical;

import net.minecraft.world.level.block.entity.BlockEntity;
import welbre.ambercraft.module.network.Master;
import welbre.ambercraft.module.network.NetworkModule;

public class ElectricalModulesMaster extends Master {
    public ElectricalModulesMaster(NetworkModule master) {
        super(master);
    }

    @Override
    protected boolean compile(NetworkModule master) {
        return false;
    }

    @Override
    protected void tick(BlockEntity entity, boolean isClientSide) {

    }
}
