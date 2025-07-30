package welbre.ambercraft.module.network;

import net.minecraft.world.level.block.entity.BlockEntity;

import java.io.Serializable;

public abstract class Master implements Serializable {
    protected boolean isCompiled = false;
    protected final NetworkModule master;

    public Master(NetworkModule master) {
        this.master = master;
    }

    protected void compile()
    {
        isCompiled = true;
    }

    public void tick(BlockEntity entity) {
        if (!isCompiled)
            compile();
    }
}
