package welbre.ambercraft.debug.network;

import welbre.ambercraft.module.network.NetworkModule;

import java.util.ArrayList;
import java.util.List;

public class OrbitalViewerSort {
    public static final int RADIUS = 150;

    public static void sort(NetworkScreen network)
    {
        int count = 0;
        NetworkModule oldestFather = network.main;
        NetworkModule temp = network.main.getFather();

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
        
        network.animations.clear();
        try
        {
            VISITED.clear();
            Helper root_helper = add(network, root, 0);
            VISITED.clear();
            move(root_helper,0);

            network.centralize();

            //update size
        } catch (Exception e) {e.printStackTrace();}
    }
    
    private static void move(Helper helper, double angle)
    {
        if (VISITED.contains(helper))
            return;
        VISITED.add(helper);

        double delta;
        if (helper.node.module.getMaster() != null)
            delta = 2.0*Math.PI / (helper.children.size());
        else
        {
            delta = 2.0 * Math.PI / (helper.children.size() + 1);
            angle += Math.PI + delta;
        }

        for (Helper child : helper.children)
        {
            double[] dir = {Math.cos(angle), Math.sin(angle)};
            double length = Math.sqrt(dir[0] * dir[0] + dir[1] * dir[1]);
            dir[0] /= length;
            dir[1] /= length;

            child.node.x = (int) (dir[0]*RADIUS) + helper.node.x;
            child.node.y = (int) (dir[1]*RADIUS) + helper.node.y;

            move(child, angle);

            angle += delta;
        }
    }
    
    private static final List<Helper> VISITED = new ArrayList<>();
    private static Helper add(NetworkScreen network, ScreenNode node, int deep)
    {
        //cyclical check
        for (Helper helper : VISITED)
            if (helper.node == node)
                return helper;

        Helper helper = new Helper(0, deep, node, new ArrayList<>());
        VISITED.add(helper);

        //recursive insertion.
        for (NetworkModule child : node.module.getChildren())
        {
            ScreenNode childNode = network.getScreenNode(child);
            assert childNode != null;

            Helper childHelper = add(network, childNode, deep + 1);
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
