package welbre.ambercraft.module.electrical;

import kuse.welbre.sim.electrical.abstractt.Element;
import welbre.ambercraft.module.network.NetworkModule;

public abstract class ElectricalModule extends NetworkModule {
    public abstract void alloc();
    public abstract Element[] compile();
    public abstract void free();
}
