package welbre.ambercraft.module.network;

import net.minecraft.world.level.block.entity.BlockEntity;

import java.io.Serializable;

/**
 * A class to handle all logic in the {@link NetworkModule}.<br>
 * Uses a flag system to call {@link Master#compile} each time that the network changes.<br>
 * The tick logic
 */
public abstract class Master implements Serializable {
    protected final NetworkModule master;
    private boolean isClean = false;

    public Master(NetworkModule master) {
        this.master = master;
    }

    /// compile the master using the NetworkModule obs the module is the master!.
    /// @return if the compilation was a success.
    protected abstract boolean compile(NetworkModule master);

    protected abstract void tick(BlockEntity entity, boolean isClientSide);

    private void compile()
    {
        if (compile(master))
            isClean = true;
    }

    public final void tick(BlockEntity entity) {
        if (!isClean)
            compile();
        if (isClean)
            tick(entity, entity.getLevel() != null && entity.getLevel().isClientSide());
    }

    public void dirt() {isClean = false;}
    public boolean isClean() {return isClean;}
}
