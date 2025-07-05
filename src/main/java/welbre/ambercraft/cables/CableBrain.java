package welbre.ambercraft.cables;

import net.minecraft.core.Direction;
import welbre.ambercraft.blockentity.FacedCableBE;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CableBrain {
    private FaceBrain up = null;
    private FaceBrain down = null;
    private FaceBrain north = null;
    private FaceBrain south = null;
    private FaceBrain west = null;
    private FaceBrain east = null;
    private final List<FaceBrain> activeFaces = new ArrayList<>();

    public CableBrain() {
    }

    private CableBrain(FaceBrain up, FaceBrain down, FaceBrain north, FaceBrain south, FaceBrain west, FaceBrain east) {
        this.up = up;
        this.down = down;
        this.north = north;
        this.south = south;
        this.west = west;
        this.east = east;
    }

    public void addCenter(Direction face, AmberFCableComponent component, FacedCableBE cable){
        FaceBrain brain = new FaceBrain(component.getType(), cable);
        activeFaces.add(brain);
        switch (face){
            case UP -> up = brain;
            case DOWN -> down = brain;
            case NORTH -> north = brain;
            case SOUTH -> south = brain;
            case EAST -> east = brain;
            case WEST -> west = brain;
        }
    }

    public void removeCenter(Direction face) {
        switch (face){
            case UP -> {
                activeFaces.remove(up);
                up = null;
            }
            case DOWN -> {
                activeFaces.remove(down);
                down = null;
            }
            case NORTH -> {
                activeFaces.remove(north);
                north = null;
            }
            case SOUTH -> {
                activeFaces.remove(south);
                south = null;
            }
            case EAST -> {
                activeFaces.remove(east);
                east = null;
            }
            case WEST -> {
                activeFaces.remove(west);
                west = null;
            }
        }
    }

    public @Nullable FaceBrain getFaceBrain(Direction face){
        return switch (face){
            case UP -> up;
            case DOWN -> down;
            case NORTH -> north;
            case SOUTH -> south;
            case EAST -> east;
            case WEST -> west;
        };
    }
}
