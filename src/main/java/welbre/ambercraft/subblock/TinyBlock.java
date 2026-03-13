package welbre.ambercraft.subblock;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Definition that creates the parts that can be used in the SubBlock.<br>
 * This class is responsible for define logic, rendering, shape, external output, and more.
 * <h6 color="yellow">Must be registered in you TinyBlockRegister!</h>
 */
public abstract class TinyBlock
{
    public VoxelShape shape;
    /// The registry name used in {@link TinyBlockRegister}
    public final ResourceLocation registerName;

    public TinyBlock(ResourceLocation registerName)
    {
        this.registerName = registerName;
    }

    public TinyBlock(ResourceLocation l, VoxelShape shape)
    {
        this.registerName = l;
        this.shape = shape;
    }

    /// returns the AABB with the bounds translated
    protected @NotNull AABB getTranslatedBounds(TinyBlockState state)
    {
        return shape.bounds().move(state.x / 16.0, state.y / 16.0, state.z / 16.0);
    }

    protected @NotNull VoxelShape getTranslatedShape(TinyBlockState state)
    {
        return state.definition.shape.move(state.x / 16.0, state.y / 16.0, state.z / 16.0);
    }

    /// Solves the shape ande return a translated list of AABB
    protected @NotNull List<AABB> getTranslatedAABB(TinyBlockState state)
    {
        return shape.toAabbs().stream().map(aabb -> aabb.move(state.x / 16.0, state.y / 16.0, state.z / 16.0)).toList();
    }

    /**
     * Gets a model for <b>static</b> rendering the item or in sub block.<br>
     *
     * <p>
     *     Static rendering is a concept used in AmberCraft subBlocks created to be similar to Minecraft's model pipe-line for blocks.<br>
     *     The result of this method is stored in an internal cache <i>in SubBlockBE</i>, and reused many times, to force the update of the model use {@link SubBlockBE#requireStaticRenderUpdate(TinyBlockState)}.<br>
     *     You don't need to call this my hand in most cases, the SubBlock do by him self when player interact with the SubBlock.
     * </p>
     *<p>
     *     The TinyBlockState state parameter is the block state that need a model to be updated. <b>The state can be null!</b> and this only happen if and only if the method is called to get an <b>ITEM!</b> model.
     *</p>
     */
    public abstract BakedModel staticModel(@Nullable TinyBlockState state);

    public abstract void dynamicRender(TinyBlockState state);

    /// Return an itemStack or null based on the parameters.<br>
    /// Use it for drop itemStack when breaks, exploded, broken with wrong tool, or others situations.
    public abstract @Nullable ItemStack getDroppedItem(TinyBlockState state, LootParams.Builder params);

    /**
     * AmberCraft uses only one item {@link welbre.ambercraft.AmberCraft.Items#TINY_ITEM} to represent all TinyBlock in the inventory.
     * To change which TinyBlock an ItemStack represents, we use the {@link welbre.ambercraft.AmberCraft.DataComponents#TINY_BLOCK_DATA_COMPONENT} to set a TinyBlock, and this method is used
     * to get the name that the ItemStack have.
     */
    public abstract @NotNull Component getTinyItemName();

    /**
     * Returns a SoundType that will be used to the default minecraft operations, like place, step, and break a TinyBlock.
     */
    public abstract @NotNull SoundType getSoundType(TinyBlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity);

    /**
     * Similar to {@link net.minecraft.world.level.block.state.BlockState#getDestroySpeed(BlockGetter, BlockPos)}, but this provides a TinyBlockState to be used.
     * @return The TinyBlock hardness
     */
    public abstract float getDestroySpeed(TinyBlockState state, BlockGetter level, BlockPos pos);
    /**
     * Similar to {@link Player#getDestroySpeed(BlockState, BlockPos)}, Used to get the destroySpeed when a player is braking a TinyBlockState.<br>
     * This method check for player enchantment in the ItemStack, if is the correct tool, for {@link net.minecraft.world.effect.MobEffect} and other things that you deem essencial.
     * @param player The player that is breaking the state.
     * @param state The state which is being broken.
     * @return The speed
     */
    public abstract float getPlayerDestroySpeed(Player player, TinyBlockState state, BlockGetter level, BlockPos pos);

    public abstract void playStepSound(@NotNull TinyBlockState tiny, @NotNull Level level, @NotNull BlockPos pos, @NotNull Entity entity);

    /**
     * Used to deal with particles in the client.
     * <p>
     *     Handle all particle cases related to blocks, Breaking, Hitting, And Stepping
     * </p>
     *
     * @param particleCase The case that is dealing with.
     * @param hitResult Isn't null only when particleCase == Hitting
     */
    @OnlyIn(Dist.CLIENT)
    public abstract void handleParticles(@NotNull ClientLevel level, @NotNull BlockPos pos, @NotNull TinyBlockState state, @NotNull ParticleEngine engine, @NotNull ParticleCase particleCase, @Nullable BlockHitResult hitResult);




    @OnlyIn(Dist.CLIENT)
    public enum ParticleCase
    {
        DESTROY, STEP, HIT
    }
}
