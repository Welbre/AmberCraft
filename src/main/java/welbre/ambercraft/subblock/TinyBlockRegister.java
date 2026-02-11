package welbre.ambercraft.subblock;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;

import java.util.function.Function;

/**
 * Register all TinyBlock used by the ambercraft mod.<br>
 * Notice that this is where we add new TinyBlock!, don't confuse with {@link AmberCraft.AmberRegisters#TINY_BLOCK_REGISTRY}, they are for new DeferredRegister creation.
 */
public enum TinyBlockRegister
{

    TEST((r) -> new TinyBlock(r, Shapes.box(0,0,0,0.2,0.2,0.2)));

    private DeferredHolder<TinyBlock, TinyBlock> tinyBlock;
    private Function<ResourceLocation, TinyBlock> factory;
    private final String registerName;

    TinyBlockRegister(Function<ResourceLocation, TinyBlock> factory)
    {
        this.factory = factory;
        registerName = AmberCraft.MOD_ID + ":" + name().toLowerCase();
    }

    public DeferredHolder<TinyBlock, TinyBlock> getHolder()
    {
        return tinyBlock;
    }

    public static final DeferredRegister<TinyBlock> REGISTER = DeferredRegister.create(AmberCraft.AmberRegisters.TINY_BLOCK_REGISTRY, AmberCraft.MOD_ID);
    static
    {
        //a simple solution to use a static field :/
        for (TinyBlockRegister value : TinyBlockRegister.values())
        {
            value.tinyBlock = REGISTER.register(value.name().toLowerCase(), value.factory);
            value.factory = null;
        }
    }

    /// Get a new instance of TinyBlock by him name, returns null if don't find it.
    public static @Nullable TinyBlock FROM_STRING(@NotNull String s)
    {
        for (TinyBlockRegister value : TinyBlockRegister.values())
        {
            if (value.registerName.equals(s))
                return value.getHolder().value();
        }

        return null;
    }
}
