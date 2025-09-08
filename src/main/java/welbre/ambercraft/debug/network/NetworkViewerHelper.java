package welbre.ambercraft.debug.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import welbre.ambercraft.blockentity.FacedCableBE;
import welbre.ambercraft.cables.CableState;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModulesHolder;
import welbre.ambercraft.module.network.NetworkModule;

import java.util.*;

public class NetworkViewerHelper {

    public static List<NetworkWidget> CREATE_ALL_WIDGETS(BlockPos initialPosition, NetworkModule[] serverModules) {
        Level level = Minecraft.getInstance().level;
        assert level != null;

        Set<ModulesHolder> scan = SCAN_LEVEL(level, initialPosition);
        ArrayList<NetworkWidget> widgets = new ArrayList<>();
        List<NetworkModule> modules = EXTRACT_MODULES(serverModules);

        for (ModulesHolder holder : scan)
            CREATE_AND_ADD_WIDGET(widgets, modules, holder, holder.getBlockPos().equals(initialPosition));

        //run after create all widgets!
        for (NetworkWidget widget : widgets)
        {
            INIT_CHILD_CONNECTION(widget, widgets);
            SET_FATHER(widget, widgets);
        }

        return widgets;
    }

    private static List<NetworkModule> EXTRACT_MODULES(NetworkModule[] serverModules) {
        Set<NetworkModule> modules = new HashSet<>();
        Set<NetworkModule> roots = new HashSet<>();

        for (NetworkModule module : serverModules)//filter to a root list
            roots.add(module.getRoot());

        for (NetworkModule root : roots)
        {
            Queue<NetworkModule> queue = new ArrayDeque<>(List.of(root));

            while (!queue.isEmpty())
            {
                NetworkModule next = queue.poll();
                if (modules.contains(next))
                    continue;

                queue.addAll(List.of(next.getChildren()));

                modules.add(next);
            }
        }

        return modules.stream().toList();
    }

    private static Set<ModulesHolder> SCAN_LEVEL(Level level, BlockPos initialPosition)
    {
        ArrayDeque<ModulesHolder> queue = new ArrayDeque<>();
        HashSet<ModulesHolder> visited = new HashSet<>();

        if (level.getBlockEntity(initialPosition) instanceof ModulesHolder holder)
            queue.push(holder);

        while (!queue.isEmpty())
        {
            ModulesHolder next = queue.pop();
            BlockPos nextPos = next.getBlockPos();

            //search
            for (var dir : Direction.values())
                if (level.getBlockEntity(nextPos.relative(dir)) instanceof ModulesHolder holder && !visited.contains(holder))
                    queue.add(holder);
            // diagonal search in the case that next is a FacedCable
            if (next instanceof FacedCableBE cable)
                for (Direction face : cable.getState().getCenterDirections())
                    for (Direction dir : CableState.GET_FACE_DIRECTIONS(face))
                        if (level.getBlockEntity(nextPos.relative(dir).relative(face)) instanceof ModulesHolder holder && !visited.contains(holder))
                            queue.add(holder);

            visited.add(next);
        }

        return visited;
    }

    private static void CREATE_AND_ADD_WIDGET(
            ArrayList<NetworkWidget> widgets,
            Collection<NetworkModule> modules,
            ModulesHolder holder,
            boolean isMain)
    {
        //OBS modulesHolder is got from the client side!
        for (Module module : holder.getModules())
            if (module instanceof NetworkModule client)
                for (var server : modules)
                    if (server.ID == client.ID)
                    {
                        widgets.add(new NetworkWidget(0, 0, server, client, holder, isMain));
                        break;
                    }
    }

    private static void INIT_CHILD_CONNECTION(NetworkWidget widget, ArrayList<NetworkWidget> widgets) {
        ArrayList<NetworkWidget> children = new ArrayList<>();

        for (NetworkModule module : widget.serverModule.getChildren())
        {
            for (NetworkWidget networkWidget : widgets)
            {
                if (networkWidget.serverModule.ID == module.ID)
                {
                    children.add(networkWidget);
                    break;
                }
            }
        }

        Connection[] connections = new Connection[children.size()];
        for (int i = 0; i < children.size(); i++)
            connections[i] = new Connection(widget, children.get(i), NetworkWidget.CHILDREN_CONNECTION_COLOR);

        widget.childConnection = connections;
    }

    private static void SET_FATHER(NetworkWidget widget, ArrayList<NetworkWidget> widgets)
    {
        if (widget.serverModule.getFather() == null)
            return;
        NetworkWidget father = null;
        for (NetworkWidget networkWidget : widgets)
            if (networkWidget.serverModule.ID == widget.serverModule.getFather().ID)
            {
                father = networkWidget;
                break;
            }
        widget.father = father;
    }

    public static List<List<NetworkWidget>> SORT_LAYERS(NetworkModule[] serverModules, List<NetworkWidget> networkWidgets) {
        Set<NetworkModule> roots = new HashSet<>();

        for (NetworkModule module : serverModules)
            roots.add(module.getRoot());

        List<List<NetworkWidget>> layers = new ArrayList<>();

        for (NetworkModule root : roots)
        {
            var layer = new ArrayList<NetworkWidget>();
            layers.add(layer);

            for (NetworkWidget widget : networkWidgets)
                if (widget.serverModule.getRoot().ID == root.ID)
                    layer.add(widget);
        }


        return layers;
    }
}
