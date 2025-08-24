package welbre.ambercraft.cables;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import welbre.ambercraft.blockentity.FacedCableBE;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.network.NetworkModule;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class CableBrain {
    /// array with length 6 contains the brains in Down, Up, North, South, West, East order.
    private final FaceBrain[] brains = new FaceBrain[6];
    private final List<FaceBrain> activeFaces = new ArrayList<>();

    public CableBrain() {
    }

    private CableBrain(FaceBrain down, FaceBrain up, FaceBrain north, FaceBrain south, FaceBrain west, FaceBrain east) {
        brains[0] = down;
        brains[1] = up;
        brains[2] = north;
        brains[3] = south;
        brains[4] = west;
        brains[5] = east;
    }

    public void addCenter(Direction face, FacedCableComponent component, FacedCableBE cable){
        FaceBrain brain = new FaceBrain(component.getType(), cable);
        activeFaces.add(brain);
        brains[face.ordinal()] = brain;
    }

    public void removeCenter(Direction face) {
        FaceBrain brain = brains[face.ordinal()];
        if (brain != null)
            for (Module module : brain.modules())
                if (module instanceof NetworkModule network)
                    network.disconnectAll();
        activeFaces.remove(brain);

        brains[face.ordinal()] = null;
    }

    public void connect(Direction face, NetworkModule[] modules)
    {
        FaceBrain faceBrain = getFaceBrain(face);
        if (faceBrain != null)
            for (Module module : faceBrain.modules())
                if (module instanceof NetworkModule networkModule)
                    for (NetworkModule networkModule1 : modules)
                        networkModule.connect(networkModule1);
    }

    public @Nullable FaceBrain getFaceBrain(Direction face){
        return brains[face.ordinal()];
    }

    public static final Codec<CableBrain> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FaceBrain.CODEC.optionalFieldOf("down").forGetter(brain -> Optional.ofNullable(brain.brains[0])),
            FaceBrain.CODEC.optionalFieldOf("up").forGetter(brain -> Optional.ofNullable(brain.brains[1])),
            FaceBrain.CODEC.optionalFieldOf("north").forGetter(brain -> Optional.ofNullable(brain.brains[2])),
            FaceBrain.CODEC.optionalFieldOf("south").forGetter(brain -> Optional.ofNullable(brain.brains[3])),
            FaceBrain.CODEC.optionalFieldOf("west").forGetter(brain -> Optional.ofNullable(brain.brains[4])),
            FaceBrain.CODEC.optionalFieldOf("east").forGetter(brain -> Optional.ofNullable(brain.brains[5]))
    ).apply(instance, (down,up,north,south,west,east) -> new CableBrain(
            down.orElse(null),
            up.orElse(null),
            north.orElse(null),
            south.orElse(null),
            west.orElse(null),
            east.orElse(null)
    )));

    /// Returns all faces that contains a brain.
    public List<Direction> getCenterDirections() {
        return Arrays.stream(Direction.values()).filter(d -> brains[d.ordinal()] != null).toList();
    }
}
