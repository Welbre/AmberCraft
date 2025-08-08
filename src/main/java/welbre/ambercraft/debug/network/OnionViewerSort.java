package welbre.ambercraft.debug.network;

import welbre.ambercraft.module.network.NetworkModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnionViewerSort {
    public static final int RADIUS = 150;

    public static void sort(NetworkScreen network)
    {
        int count = 0;
        NetworkModule oldestFather = network.main.get(network.selectedLayer);
        NetworkModule temp = network.main.get(network.selectedLayer).getFather();

        while (temp != null){
            oldestFather = temp;
            temp = temp.getFather();

            if (count++ > 50000)
                throw new IllegalStateException("Circular dependency detected!");
        }

        ScreenNode root = network.getScreenNode(oldestFather);
        if (root == null)
        {
            new RuntimeException("Root node is null!").printStackTrace();
            return;
        }


        Map<Integer, List<Helper>> layers = new HashMap<>();
        network.animations.clear();
        try
        {
            VISITED.clear();
            Helper root_helper = add(network, root, layers,0);
            VISITED.clear();
            move(root_helper,layers);

            network.centralize();

            //update size
        } catch (Exception e) {e.printStackTrace();}
    }

    private static void move(Helper helper, Map<Integer, List<Helper>> layers)
    {
        if (VISITED.contains(helper))
            return;
        VISITED.add(helper);

        for (Map.Entry<Integer, List<Helper>> entry : layers.entrySet())
        {
            var deep = entry.getKey();
            var layer = entry.getValue();

            var angle = 0.0;
            var deltaAngle = 2.0*Math.PI / layer.size();

            for (Helper child : layer)
            {
                double[] dir = {Math.cos(angle), Math.sin(angle)};
                child.node.x = (int) (dir[0]*deep*RADIUS) + helper.node.x;
                child.node.y = (int) (dir[1]*deep*RADIUS) + helper.node.y;

                angle += deltaAngle;
            }
        }
    }
    
    private static final List<Helper> VISITED = new ArrayList<>();
    private static Helper add(NetworkScreen network, ScreenNode node, Map<Integer,List<Helper>> layers, int deep)
    {
        //cyclical check
        for (Helper helper : VISITED)
            if (helper.node == node)
                return helper;

        Helper helper = new Helper(0, deep, node, new ArrayList<>());
        VISITED.add(helper);
        layers.putIfAbsent(deep, new ArrayList<>());
        layers.get(deep).add(helper);

        //recursive insertion.
        for (NetworkModule child : node.module.getChildren())
        {
            ScreenNode childNode = network.getScreenNode(child);
            assert childNode != null;

            Helper childHelper = add(network, childNode, layers,deep + 1);
            helper.children.add(childHelper);
        }

        return helper;
    }

    private static final class Helper {
        int deep;
        ScreenNode node;
        List<Helper> children;

        private Helper(int size, int deep, ScreenNode node, List<Helper> children) {
            this.deep = deep;
            this.node = node;
            this.children = children;
        }
    }
}
