package welbre.ambercraft.module.electrical;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.elements.Resistor;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.level.block.entity.BlockEntity;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.network.Master;
import welbre.ambercraft.module.network.NetworkModule;

import java.util.*;

public class ElectricalModulesMaster extends Master {
    public transient AutoGroundingCircuit circuit;

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

            if (next instanceof ElectricalTerminalModule)
                throw new IllegalStateException("ElectricalTerminalModule is not a valid module for an ElectricalModuleMaster!");

            if (next instanceof ElectricalModule eem)//add all elements
                elements.addAll(Arrays.asList(eem.compile()));

            queue.addAll(Arrays.asList(next.getNeighbors()));
        }

        //the voltage sources are using the same pins, causing an infinite loop with no resistence and a matrix singular exception
        circuit = new AutoGroundingCircuit();
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

    /**
     * The main go of this class is when the circuit is checking for inconsistency in {@link Circuit#clean()}, instead of throw an exception
     * in the case that only 1 pin of the element is connected, they now create a new high resistence resistor connected to the pin and the ground,
     * therefore, the circuit can create the matrix, with minimus impact on the circuit.<br>
     */
    public static class AutoGroundingCircuit extends Circuit
    {
        @Override
        protected void checkInconsistencies() {
            //initiation
            Pin gnd = new Pin();
            HashMap<Pin, List<Element>> elements_per_pin = new HashMap<>();

            //All pins to map
            elements_per_pin.put(gnd, new ArrayList<>());
            for (Pin pin : this.analyseResult.pins)
                elements_per_pin.put(pin, new ArrayList<>());

            //Add all elements to correspondent pins.
            for (Element element : getElements())
                for (Pin pin : element.getPins())
                    elements_per_pin.get(pin == null ? gnd : pin).add(element);

            boolean changePins = false;

            //ungrounded check
            //the ground is extremely important to the solver, if isn't presente set the pin with more elements to be the ground.
            if (elements_per_pin.get(gnd).isEmpty())//check if the ground is empty
            {
                Collection<List<Element>> values = elements_per_pin.values();
                values = values.stream().sorted((a,b) -> Integer.compare(b.size(), a.size())).toList();

                final Circuit.Pin controlPin = new Pin();
                List<Element> biggest = values.iterator().next();//<- is the biggest because the values is sorted above.
                Circuit.Pin biggestPin = controlPin;

                for (Map.Entry<Pin, List<Element>> entry : elements_per_pin.entrySet())
                {
                    if (entry.getValue() == biggest)
                    {
                        biggestPin = entry.getKey();
                        break;
                    }
                }

                if (biggestPin == controlPin)
                    throw new IllegalStateException("Biggest pin can't be founded");

                //set the elements pin to the ground
                biggestPin.address = gnd.address;
                elements_per_pin.remove(gnd);
                gnd = biggestPin;
                changePins = true;
            }

            for (Map.Entry<Pin, List<Element>> entry : elements_per_pin.entrySet())
            {
                //is connected only to one element.
                if (entry.getValue().size() == 1 && entry.getKey() != gnd)
                {
                    Resistor resistor = new Resistor(entry.getKey(), null, 10e9);//1 gigaOhm
                    entry.getValue().add(resistor);
                    elements_per_pin.get(gnd).add(resistor);
                    addElement(resistor);
                    changePins = true;
                }
            }

            //Error check
            for (Map.Entry<Pin, List<Element>> entry : elements_per_pin.entrySet()) {
                Pin key = entry.getKey(); List<Element> list = entry.getValue();

                if (entry.getValue().isEmpty())
                    throw new IllegalStateException(String.format("%s have no connections! possible fault in circuit formation.", key));

                if (entry.getValue().size() == 1) {
                    Element element = list.getFirst();
                    throw new IllegalStateException(String.format("%s[%s,%s] is connected to %s without a path, possible fault in circuit formation!",
                            element.getClass().getSimpleName(),
                            element.getPinA() == null ? "gnd" : element.getPinA().address,
                            element.getPinB() == null ? "gnd" : element.getPinB().address,
                            key));
                }
            }
        }
    }

    @Override
    protected void tick(BlockEntity entity, boolean isClientSide) {
        Profiler.get().push("ElectricalModuleMaster tick");

        Profiler.get().pop();
    }
}
