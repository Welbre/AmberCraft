package welbre.ambercraft.module.heat;

import net.minecraft.world.level.block.entity.BlockEntity;
import welbre.ambercraft.module.network.Master;
import welbre.ambercraft.module.network.NetworkModule;
import welbre.ambercraft.sim.heat.HeatNode;

import java.util.*;

public class HeatModuleMaster extends Master {
    transient Link[] links = new Link[0];
    transient HeatNode[] nodes = new HeatNode[0];

    public HeatModuleMaster(NetworkModule networkModule) {
        super(networkModule);
    }

    @Override
    public boolean compile(NetworkModule master, boolean isClientSide) {
        if (isClientSide)
            return true;//if is the client just skip.
        if (! (master instanceof HeatModule) )
            throw new IllegalStateException("HeatModuleMaster can only be used with HeatModule!");

        Queue<HeatModule> queue = new ArrayDeque<>(List.of((HeatModule) master));
        List<Link> links = new ArrayList<>();

        while (true)
        {
            HeatModule next = queue.poll();
            if (next == null) break;

            for (NetworkModule neighbor : next.getNeighbors())
            {
                if (neighbor instanceof HeatModule heatModule)
                {
                    Link link = new Link(next.getHeatNode(), heatModule.getHeatNode());
                    if (links.contains(link))
                        continue;
                    links.add(link);
                    if (heatModule.getNeighborCount() > 1)//if is 0 don't need to add, and if is 1, is only connected to the nxt, therefore, don't need to add either.
                        if (!queue.contains(heatModule))
                            queue.add(heatModule);
                }
            }
        }

        Set<HeatNode> nodes = new HashSet<>(List.of(((HeatModule) master).getHeatNode()));
        //get all nodes in the network
        for (Link l : links)
        {
            nodes.add(l.a);
            nodes.add(l.b);
        }

        this.nodes = nodes.toArray(new HeatNode[0]);
        this.links = links.toArray(new Link[0]);

        return true;
    }

    @Override
    protected void tick(BlockEntity entity, boolean isClientSide) {
        if (!isClientSide)
        {
            for (var n : nodes)
                n.computeSoftHeatToEnvironment();
            for (var l : links)
                l.a.computeSoftHeatWithChildren(l.b);
            for (var n : nodes)
                n.updateSoftHeat();
        }
    }

    record Link(HeatNode a, HeatNode b){
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Link(HeatNode a1, HeatNode b1))
                return (a == a1 && b == b1 ) || (a == b1 && b == a1);
            return false;
        }
    }
}
