package welbre.ambercraft.subblock;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.client.RenderHelper;
import welbre.ambercraft.subblock.layz.SimpleTinyBlock;

import java.util.List;
import java.util.function.Function;

/**
 * Register all TinyBlock used by the ambercraft mod.<br>
 * Notice that this is where we add new TinyBlock!, don't confuse with {@link AmberCraft.AmberRegisters#TINY_BLOCK_REGISTRY}, they are for new DeferredRegister creation.
 */
public enum TinyBlockRegister
{

    GOLD((r) -> new SimpleTinyBlock(r, 5, Blocks.GOLD_BLOCK)),
    IRON((r) -> new SimpleTinyBlock(r, 5, Blocks.IRON_BLOCK)),
    COPPER((r) -> new SimpleTinyBlock(r, 6, Blocks.COPPER_BLOCK)),
    COAL((r) -> new SimpleTinyBlock(r, 6, Blocks.COAL_BLOCK)),
    EMERALD((r) -> new SimpleTinyBlock(r, 6, Blocks.EMERALD_BLOCK)),
    BLACK_WOOL((r) -> new SimpleTinyBlock(r, 5, Blocks.BLACK_WOOL)),
    DEBUG((r) -> new SimpleTinyBlock(r, 1, Blocks.PURPLE_WOOL));

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

    public @NotNull TinyBlock get(){ return tinyBlock.get();}

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
