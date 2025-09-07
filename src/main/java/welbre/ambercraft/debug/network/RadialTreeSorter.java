package welbre.ambercraft.debug.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.module.network.NetworkModule;

import java.util.*;
import java.util.stream.Collectors;

/***
 * Sort the network in a radial tree;
 */
public class RadialTreeSorter implements NetworkWidgetSorter {
    private static boolean PRINT_DEBUG_TRACE = false;

    @Override
    public AbstractWidget sort(Button button, NetworkViewerScreen screen)
    {
        if (screen.layer == -1)//can sort if all layers is on display.
            return null;
        List<NetworkWidget> widgets = screen.getVisibleWidgets();
        if (widgets.isEmpty()) return null;

        NetworkWidget main = NetworkViewerScreen.GET_WIDGET(widgets, screen.serverModules[screen.layer]);
        if (main == null)
            throw new IllegalStateException("Main widget is null!");

        // sort by orbital
        HashMap<Integer, ArrayList<NetworkWidget>> map = new HashMap<>();
        {
            HashSet<NetworkWidget> visited = new HashSet<>();
            //computes the distance to the main and store by distance in the map
            for (NetworkWidget widget : widgets)
            {
                if (visited.contains(widget))
                    continue;
                // computes the distance in nodes between widget and main.
                final int orbit = COMPUTE_ORBIT(widget.serverModule, main.serverModule);

                final List<NetworkWidget> orbital = map.computeIfAbsent(orbit, k -> new ArrayList<>());
                orbital.add(widget);
                visited.add(widget);
            }
        }

        //FIXME refactor this, at the moment isn't using a "local" space in the orbit, instead, the code is only taking the total space used in the orbit to compute the range.
        //start the sort itself
        final Vec2 center = new Vec2(screen.width / 2f, screen.height / 2f);
        double radius = 0;
        Orbital orbital = new Orbital(center, screen);

        //solve first and second orbit "special case"
        {
            main.setPosition((int) (center.x - main.getWidth() / 2f), (int) (center.y - main.getHeight() / 2f));

            List<NetworkWidget> children = map.get(1);

            final double delta = Math.PI * 2 / (main.serverModule.getChildren().length + (main.serverModule.getFather() == null ? 0 : 1));
            double teta = - delta / 2.0;
            radius = children.stream().mapToDouble(RadialTreeSorter::computeDiagonal).max().orElse(0) + 30;
            for (NetworkWidget child : children)
            {
                child.setPosition((int) (center.x + Math.cos(teta) * radius - child.getWidth() / 2f), (int) (center.y + Math.sin(teta) * radius - child.getHeight() / 2f));
                teta += delta;
            }
            orbital.addOrbit(radius);
        }

        for (int orbit = 2; orbit < map.size(); orbit++)//sort using the preview to get a direction.
        {
            final int previewOrbitalSize = map.get(orbit-1).size();
            final double availableAnglePerPreview = Math.PI * 2 - ((previewOrbitalSize-1)*30.0) / previewOrbitalSize;
            double biggestDiagonal = 0;
            double totalSpaceNeeded = 0;
            HashMap<NetworkWidget, List<NetworkWidget>> nextOrbitMap = new HashMap<>();

            for (NetworkWidget preview : map.get(orbit-1))
            {
                List<NetworkWidget> children = GET_CHILDREN(preview, widgets);
                NetworkWidget father = preview.serverModule.getFather() != null ? NetworkViewerScreen.GET_WIDGET(widgets, preview.serverModule.getFather()) : null;

                List<NetworkWidget> subSpace = children.stream().filter(map.get(orbit)::contains).collect(Collectors.toList());
                if (father != null && map.get(orbit).contains(father))
                    subSpace.add(father);

                nextOrbitMap.put(preview, subSpace);
                final double localSpaceNeed = subSpace.stream().mapToDouble(RadialTreeSorter::computeDiagonal).sum() + (subSpace.size() - 1) * 15;
                totalSpaceNeeded += localSpaceNeed;

                final double localDiagonal = subSpace.stream().mapToDouble(RadialTreeSorter::computeDiagonal).max().orElse(0);
                if (biggestDiagonal < localDiagonal)
                    biggestDiagonal = localDiagonal;
            }

            if (totalSpaceNeeded > Math.PI * 2 * radius)
                radius = totalSpaceNeeded / Math.PI * 2;

            radius += biggestDiagonal + 10;
            orbital.addOrbit(radius);

            for (Map.Entry<NetworkWidget, List<NetworkWidget>> entry : nextOrbitMap.entrySet())
            {
                if (entry.getValue().isEmpty())
                    continue;
                var preview = entry.getKey();
                final Vec2 dirToCenter = new Vec2(preview.getCenter()[0], preview.getCenter()[1]).add(center.negated()).normalized();
                final double hAngle = Math.atan2(dirToCenter.y, dirToCenter.x);
                //final double localSpace = entry.getValue().stream().mapToDouble(RadialTreeSorter::computeDiagonal).sum() + entry.getValue().size() * 15.0;
                final double localSpace = entry.getValue().stream().mapToDouble(RadialTreeSorter::computeDiagonal).sum();
                final double spaceArc = 15.0 / radius;
                final double totalArc = localSpace / radius;
                double teta = hAngle - totalArc/2;

                if (PRINT_DEBUG_TRACE)
                {
                    int color = new Random().nextInt(0XFFFFFF) | (0xff << 24);
                    screen.temp.add(RadialTreeSorter.DRAW_LINE(center,hAngle, radius, 0xFF0000FF));
                    screen.temp.add(RadialTreeSorter.DRAW_LINE(center,hAngle-totalArc/2, radius, color));
                    screen.temp.add(RadialTreeSorter.DRAW_LINE(center,hAngle+totalArc/2, radius, color));
                }

                for (var child : entry.getValue())
                {
                    var space = RadialTreeSorter.computeDiagonal(child) / (2*radius);
                    teta += space;
                    final int x = (int) (center.x + Math.cos(teta)*radius);
                    final int y = (int) (center.y + Math.sin(teta)*radius);
                    if (PRINT_DEBUG_TRACE)
                    {
                        screen.temp.add((guiGraphics, mouseX, mouseY, partialTick) -> {
                            guiGraphics.drawCenteredString(Minecraft.getInstance().font, "0", x, y, 0xFFFF0000);
                        });
                    }
                    child.setPosition(Math.round(x - child.getWidth()/2f), Math.round(y - child.getHeight()/2f));
                    teta += space;
                }
            }
        }

        return orbital;
    }

    private static Renderable DRAW_LINE(Vec2 p0, double angle, double radius, int color)
    {
        return DRAW_LINE(p0, new Vec2((float) (p0.x + Math.cos(angle)*radius), (float) (p0.y + Math.sin(angle)*radius)), color);
    }
    private static Renderable DRAW_LINE(Vec2 p0, Vec2 p1, int color)
    {
        return (GuiGraphics graphics, int mouseX, int mouseY, float partialTick) ->
                Connection.drawLine(graphics, Math.round(p0.x), Math.round(p0.y), Math.round(p1.x), Math.round(p1.y), color);
    }

    private int COMPUTE_ORBIT(NetworkModule module, NetworkModule center) {
        HashSet<NetworkModule> visited = new HashSet<>();
        return COMPUTE_ORBIT_HELPER(module, center, 0, visited);
    }

    private int COMPUTE_ORBIT_HELPER(NetworkModule module, NetworkModule center, int orbit, HashSet<NetworkModule> visited)
    {
        if (module.ID == center.ID)
            return orbit;
        if (visited.contains(module))
            return -1;
        visited.add(module);
        for (NetworkModule child : module.getChildren())
        {
            int childOrbit = COMPUTE_ORBIT_HELPER(child, center, orbit+1, visited);
            if (childOrbit != -1)
                return childOrbit;
        }
        if (module.getFather() != null)
        {
            if (module.getFather().ID == center.ID)
                return orbit+1;

            int fatherOrbit = COMPUTE_ORBIT_HELPER(module.getFather(), center, orbit+1, visited);
            if (fatherOrbit != -1)
                return fatherOrbit;
        }
        return -1;
    }

    private static List<NetworkWidget> GET_CHILDREN(NetworkWidget target, List<NetworkWidget> widgets)
    {
        List<NetworkWidget> children = new ArrayList<>();
        for (NetworkModule child : target.serverModule.getChildren())
        {
            NetworkWidget widget = NetworkViewerScreen.GET_WIDGET(widgets, child);
            if (widget == null)
                throw new IllegalStateException("Child widget is null!");
            children.add(widget);
        }
        return children;
    }

    private static Double computeDiagonal(NetworkWidget widget)
    {
        return (double) new Vec2(widget.getWidth(), widget.getHeight()).length();
    }

    public static final class Orbital extends AbstractWidget
    {
        ArrayList<Double> radii = new ArrayList<>();

        public Orbital(Vec2 center, Screen screen) {
            super((int) center.x, (int) center.y, screen.width, screen.height, NetworkWidgetSorter.NAME);
        }

        @Override
        protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            for (Double radius : radii)
            {
                if (radius == 0)
                    continue;

                double preview = 0;
                for (double teta = Math.PI * 2 / 64; teta < Math.PI * 2.0 + Math.PI * 2 / 64; teta += Math.PI * 2 / 64)
                {
                    Connection.drawLineAA(guiGraphics,
                            (int) (getX() + Math.cos(preview) * radius),
                            (int) (getY() + Math.sin(preview) * radius),
                            (int) (getX() + Math.cos(teta) * radius),
                            (int) (getY() + Math.sin(teta) * radius),
                            0x99FFFFFF
                            );
                    preview = teta;
                }
            }
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

        }

        public void addOrbit(double radius) {
            radii.add(radius);
        }
    }
}
