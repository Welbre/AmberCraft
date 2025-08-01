package welbre.ambercraft.module.heat;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.module.network.Master;
import welbre.ambercraft.module.network.NetworkModule;
import welbre.ambercraft.sim.heat.HeatNode;

import java.io.Serializable;
import java.util.*;

public class HeatModuleMaster extends Master {
    transient Connections connections;

    public HeatModuleMaster(NetworkModule networkModule) {
        super(networkModule);
    }

    @Override
    protected void compile() {
        super.compile();

        int count = 0;
        ArrayList<HeatModule> visited = new ArrayList<>();
        Stack<HeatModule> stack = new Stack<>();
        LinkBuilder builder = new LinkBuilder();

        stack.push((HeatModule) this.master);

        while (!stack.isEmpty())
        {
            HeatModule next = stack.pop();
            builder.addNode(next.node);

            for (NetworkModule child : next.getChildren())
            {
                if (child instanceof HeatModule heatChild)
                {
                    builder.addLink(next.node, heatChild.node);
                    if (visited.contains(child))
                        continue;
                    visited.add(heatChild);
                    stack.push(heatChild);
                }
            }

            if (count++ > 1_000_000)
            {
                CRASH(next, stack, builder.build());
                return;
            }
        }

        connections = builder.build();
    }

    @Override
    public void tick(BlockEntity entity) {
        if (entity.getLevel() != null)
            if (!entity.getLevel().isClientSide())//run only on the server side
            {
                super.tick(entity);
                connections.tick();
            }
    }

    private void CRASH(NetworkModule current, Stack<? extends NetworkModule> stack, Connections connections)
    {
        AmberCraft.LOGGER.warn("Master disabled due to circular dependency!", new IllegalStateException("Master %s (@%x) disabled, duo circular dependency with ticking %s (@%x)!".formatted(
                master.getClass().getSimpleName(), master.ID,
                current.getClass().getSimpleName(), current.ID
        )));

        StringBuilder stackBuilder = new StringBuilder();
        HashSet<NetworkModule> visited = new HashSet<>();
        visited.add(master);

        for (NetworkModule a : stack)
        {
            stackBuilder.append(a.toString()).append("\n");
            if (visited.contains(a))
                break;
            visited.add(a);
        }
        StringBuilder linksBuilder = new StringBuilder();
        for (Link link : connections.links)
            linksBuilder.append(link).append("\n");

        AmberCraft.LOGGER.info("Queue Status:\n master -> {}\t current-> {}\nstack:\n{}##redundancy##\nlinks:\n{}", master, current, stackBuilder, linksBuilder);

        AmberCraft.LOGGER.error("Fail while ticking!", new IllegalStateException("Circular dependency detected while ticking!"));
        this.dirt();
    }

    private record Link(HeatNode father, HeatNode[] child) implements Serializable {
        @Override
        public @NotNull String toString() {
            return "Link@%x : father = %s, child = %s".formatted(hashCode(),father, child);
        }
    }

    private record Connections(List<Link> links, List<HeatNode> nodes) {
        public void tick() {
            for (HeatNode node : nodes)
                node.computeSoftHeatToEnvironment();
            for (Link link : links)
                link.father.computeSoftHeatWithChildren(link.child);
            for (HeatNode node : nodes)
                node.updateSoftHeat();
        }
    }

    private static final class LinkBuilder extends HashMap<HeatNode, ArrayList<HeatNode>>
    {
        private final HashSet<HeatNode> set = new HashSet<>();

        public void addLink(HeatNode father, HeatNode child){
            this.putIfAbsent(father, new ArrayList<>());
            this.get(father).add(child);
        }

        public Connections build(){
            ArrayList<Link> links = new ArrayList<>();
            for (Entry<HeatNode, ArrayList<HeatNode>> entry : entrySet())
            {
                links.add(new Link(entry.getKey(), entry.getValue().toArray(new HeatNode[0])));
                set.add(entry.getKey());
                set.addAll(entry.getValue());
            }

            return new Connections(links, set.stream().toList());
        }

        public void addNode(HeatNode node) {
            set.add(node);
        }
    }
}
