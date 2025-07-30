package welbre.ambercraft.debug;

import welbre.ambercraft.module.heat.HeatModule;
import welbre.ambercraft.module.network.NetworkModule;

import java.util.*;

class TreeViewerSort {
    public static int Y_MARGIN = 30;
    public static int X_MARGIN = 30;

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
        Helper root_helper;

        network.animations.clear();

        try
        {
            VISITED.clear();
            root_helper = add(network, root, 0);
            //update size
            VISITED.clear();
            updateSize(root_helper);
            VISITED.clear();
            move(root_helper, network.width/2,Y_MARGIN);

        } catch (Exception e) {e.printStackTrace();}
    }

    private static void updateSize(Helper helper)
    {
        if (VISITED.contains(helper))
            return;
        VISITED.add(helper);

        for (var child : helper.children)
            updateSize(child);


        if (helper.children.isEmpty())
            helper.size = helper.node.width;
        else
        {
            for (var child : helper.children)
                helper.size += child.size + X_MARGIN;
            helper.size -= X_MARGIN;
        }
    }

    private static void move(Helper helper, int x, int y)
    {
        //cyclical check
        if (VISITED.contains(helper))
            return;
        VISITED.add(helper);

        int board = x -(helper.size / 2);
        int moved = 0;
        for (Helper child : helper.children)
        {
            moved += child.size / 2;//center
            move(child, board + moved, y);//drawn the next layer
            moved += child.size / 2;//move to the end
            moved += X_MARGIN;//add space
        }

        helper.node.x = x;
        helper.node.y = y + helper.deep * Y_MARGIN * 3;
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
        int size;
        int deep;
        ScreenNode node;
        List<Helper> children;

        private Helper(int size, int deep, ScreenNode node, List<Helper> children) {
            this.size = size;
            this.deep = deep;
            this.node = node;
            this.children = children;
        }
    }
}
