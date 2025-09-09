package welbre.ambercraft.module.electrical;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Element;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.level.block.entity.BlockEntity;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.network.Master;
import welbre.ambercraft.module.network.NetworkModule;

import java.util.*;

public class ElectricalModulesMaster extends Master {
    public transient Circuit circuit;

    public ElectricalModulesMaster(NetworkModule master) {
        super(master);
    }

    @Override
    protected boolean compile(NetworkModule master, boolean isClientSide) {
        if (isClientSide)
            //skip the client side matrix formation.
            //only the server will have the circuit if the client wants to do any task in the circuit, this must be done via Packets
            //this is extremely important, creating a circuit is expensive, redundant tasks should be avoided.
            return true;
        Profiler.get().push("ElectricalModuleMaster compile");

        Set<Element> elements = new HashSet<>();
        Set<NetworkModule> visited = new HashSet<>();
        Queue<NetworkModule> queue = new ArrayDeque<>();
        queue.add(master);

        for (;;)
        {
            //loop control
            NetworkModule next = queue.poll();
            if (next == null) break;
            if (visited.contains(next)) continue;
            visited.add(next);

            if (next instanceof ElectricalModule em)
                elements.add(em.getElement());
            else if (next instanceof ElectricalCableModule ecm)
                elements.addAll(Arrays.asList(ecm.getResistors()));

            queue.addAll(Arrays.asList(next.getChildren()));
        }

        circuit = new Circuit();
        circuit.addElement(elements);

        try
        {
            circuit.preCompile();
        } catch (Exception e)
        {
            AmberCraft.LOGGER.warn("ElectricalModuleMaster failed to preCompile!", e);
            Profiler.get().pop();
            return true;
        }

        Profiler.get().pop();
        return true;
    }

    @Override
    protected void tick(BlockEntity entity, boolean isClientSide) {
        Profiler.get().push("ElectricalModuleMaster tick");

        Profiler.get().pop();
    }
}
