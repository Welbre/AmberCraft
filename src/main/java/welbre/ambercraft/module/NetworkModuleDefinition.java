package welbre.ambercraft.module;

import net.minecraft.world.level.block.entity.BlockEntity;
import welbre.ambercraft.sim.network.Network;

import java.util.function.Consumer;

public abstract class NetworkModuleDefinition<T extends Module, K extends BlockEntity, J extends Network.Node> implements ModuleDefinition<T,K> {
    private final Consumer<J> setter;

    public NetworkModuleDefinition(Consumer<J> setter) {
        this.setter = setter;
    }

    public Consumer<J> getSetter() {
        return setter;
    }
}
