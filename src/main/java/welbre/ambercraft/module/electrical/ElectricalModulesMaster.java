package welbre.ambercraft.module.electrical;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.elements.Resistor;
import net.minecraft.world.level.block.entity.BlockEntity;
import welbre.ambercraft.module.network.Master;
import welbre.ambercraft.module.network.NetworkModule;

import java.util.*;

public class ElectricalModulesMaster extends Master {
    public transient Circuit circuit;

    public ElectricalModulesMaster(NetworkModule master) {
        super(master);
    }

    @Override
    protected boolean compile(NetworkModule master) {
        if (master.getFather() != null)
            throw new IllegalStateException("Master isn't the root!");

        Map<ElectricalCableModule, Circuit.Pin> cablePins = new HashMap<>();
        List<ElectricalPinModule> pinModules = new ArrayList<>();
        List<Element> electricalElements = new ArrayList<>();
        List<Resistor> cablesResistor = new ArrayList<>();
        Queue<NetworkModule> queue = new ArrayDeque<>();
        queue.add(master);

        //create all pins
        for (;;)
        {
            //loop control
            NetworkModule next = queue.poll();
            if (next == null) break;

            if (next instanceof ElectricalCableModule cable)
            {
                //infinite loop check
                if (cablePins.containsKey(next)) continue;
                queue.addAll(List.of(next.getChildren()));
                cablePins.put(cable, new Circuit.Pin());
            } else if (next instanceof ElectricalPinModule pin)
            {
                pinModules.add(pin);
                queue.addAll(List.of(pin.getChildren()));
            }
        }

        //create all resistors between the cables, and the cable|element connection, and connected them!
        for (Map.Entry<ElectricalCableModule, Circuit.Pin> entry : cablePins.entrySet())
        {
            final Circuit.Pin a = entry.getValue();

            for (NetworkModule child : entry.getKey().getChildren())
            {
                if (child instanceof ElectricalCableModule cable)
                {
                    final Circuit.Pin b = cablePins.get(child);
                    final double resistence = (entry.getKey().resistence + cable.resistence) / 2.0;
                    cablesResistor.add(new Resistor(a, b, resistence));
                } else if (child instanceof ElectricalPinModule pin)
                {
                    final Circuit.Pin b = pin.getPin();
                    final double resistence= (entry.getKey().resistence / 2.0);
                    cablesResistor.add(new Resistor(a, b, resistence));
                }
            }
        }

        //get other elements in the network.
        for (ElectricalPinModule module : pinModules)
            for (Element element : module.getElements())
                if (! electricalElements.contains(element))
                    electricalElements.add(element);


        //create the circuit
        circuit = new Circuit();
        circuit.addElement(cablesResistor);
        circuit.addElement(electricalElements);

        try
        {
            circuit.preCompile();
            return true;
        } catch (Exception ignored)
        {
            return true;
        }
    }

    @Override
    protected void tick(BlockEntity entity, boolean isClientSide) {

    }
}
