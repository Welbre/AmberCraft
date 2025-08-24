package welbre.ambercraft.cables;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import welbre.ambercraft.blockentity.FacedCableBE;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.network.NetworkModule;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public record FaceBrain(Module[] modules) {

    public FaceBrain(CableType type, FacedCableBE cable) {
        this(type.createModules(cable));
    }

    public static final Codec<FaceBrain> CODEC = RecordCodecBuilder.create(face ->
        face.group(
                CompoundTag.CODEC.fieldOf("modules").forGetter(FaceBrain::getCompoundTag)
        ).apply(face, FaceBrain::unLoad)
    );

    private static FaceBrain unLoad(CompoundTag main) {
        final int length = main.getInt("length");
        final Module[] modules = new Module[length];

        try
        {
            for (int i = 0; i < length; i++)
            {
                Class<?> clazz = Class.forName(main.getString(String.valueOf(i)));

                Object object = clazz.getConstructor().newInstance();

                if (object instanceof Module module)
                    modules[i] = module;
                else
                    throw new IllegalArgumentException("Class %s isn't a Module class!".formatted(clazz.getName()));
            }
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return new FaceBrain(modules);
    }

    private CompoundTag getCompoundTag() {
        CompoundTag main = new CompoundTag();//all modules
        for (int i = 0; i < modules.length; i++)
            main.putString(String.valueOf(i), modules[i].getClass().getName());

        main.putInt("length", modules.length);
        return main;
    }

    public void connectModules(Module[] modules) {
        for (Module module : this.modules)
            if (module instanceof NetworkModule network)
                for (Module module0 : modules)
                    if (module0 instanceof NetworkModule network0)
                        network.connect(network0);
    }
}
