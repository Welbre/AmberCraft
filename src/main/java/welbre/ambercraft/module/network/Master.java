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
    protected abstract boolean compile(NetworkModule master, boolean isClientSide);

    protected abstract void tick(BlockEntity entity, boolean isClientSide);

    public final void tick(BlockEntity entity) {
        final boolean isClientSide = entity.getLevel() != null && entity.getLevel().isClientSide();
        if (!isClean)
            isClean = compile(master,isClientSide);

        if (isClean)
            tick(entity, entity.getLevel() != null && entity.getLevel().isClientSide());
    }

    public void dirt() {isClean = false;}
    public boolean isClean() {return isClean;}
}
