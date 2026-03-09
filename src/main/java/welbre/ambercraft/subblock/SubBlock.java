package welbre.ambercraft.subblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static welbre.ambercraft.AmberCraft.MOD_ID;

/**
 * Is the main block used by ambercraft cables and pipes; Roughly is a 16 * 16 * 16 block, that {@link TinyBlock} can be added to it.<br>
 * All rending, logic, events is handled by the {@link SubBlockBE}. To achieve this, all logic is allocated in the BlockEntity due to the minecraft
 * rigidity in the Block/BlockState system.
 */
public class SubBlock extends Block implements EntityBlock
{
    public static boolean IS_REQUIRING_STEP_SOUND = false;

    public SubBlock(Properties properties) {
        super(properties.destroyTime(0.5f));
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context)
    {
        if (level.getBlockEntity(pos) instanceof SubBlockBE be)
            return be.shape();
        else
            return Shapes.empty();
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new SubBlockBE(pos, state);
    }

    @Override
    public void stepOn(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Entity entity)
    {
        //todo implement a way yo play the correct sound.
        /*
        float f = DeltaTracker.ONE.getGameTimeDeltaPartialTick(!level.tickRateManager().isEntityFrozen(entity));
        if (level.getBlockEntity(pos) instanceof SubBlockBE be)
            for (var tiny : be.getTinyStates())
                for (AABB aabb : tiny.getTranslatedAABB())
                    if (aabb.contains(entity.getPosition(f).subtract(pos.getX(), pos.getY(), pos.getZ()).subtract(0, 1 / 32f, 0)))//half of a tinyblock in -y dir
                    {
                        tiny.definition.playStepSound(tiny, level, pos, entity);
                        return;
                    }

         */
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public @NotNull SoundType getSoundType(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos, @Nullable Entity entity)
    {
        SoundType type = super.getSoundType(state, level, pos, entity);
        if (IS_REQUIRING_STEP_SOUND && level.getBlockEntity(pos) instanceof SubBlockBE sub && entity != null)
        {
            TinyBlockState tiny = sub.getTinyStateAboveEntity(entity);
            if (tiny != null)
                return new DeferredSoundType(type.volume, type.pitch, type::getBreakSound,() -> tiny.definition.getSoundType(tiny, level, pos, entity).getStepSound(), type::getPlaceSound, type::getHitSound, type::getFallSound);
        }
        return new DeferredSoundType(type.volume, type.pitch, type::getBreakSound, () -> SoundEvents.EMPTY, type::getPlaceSound, type::getHitSound, type::getFallSound);
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, LevelReader level, @NotNull ScheduledTickAccess scheduledTickAccess, @NotNull BlockPos pos, @NotNull Direction direction, @NotNull BlockPos neighborPos, @NotNull BlockState neighborState, @NotNull RandomSource random)
    {
        //re-compute all occlusions
        if (level.getBlockEntity(pos) instanceof SubBlockBE sub)
            sub.updateOcclusion(direction);
        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------
    //-------------------------------------------------------Breaking----------------------------------------------------------------------------
    //-------------------------------------------------------------------------------------------------------------------------------------------

    /// Server Events related to the SubBlock breaking system.
    @EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = MOD_ID)
    public static final class EventHandler
    {

        /// Used to set which block is being break
        @SubscribeEvent
        public static void onPlayerInteractWithLeftClickInBlock(PlayerInteractEvent.LeftClickBlock event)
        {
            if (event.getAction() == PlayerInteractEvent.LeftClickBlock.Action.START)
                if (event.getLevel().getBlockEntity(event.getPos()) instanceof SubBlockBE be)
                {
                    //marks which block is being broken to later use.
                    TinyBlockState tiny;
                    if ((tiny = be.getTinyStateByRayCast(event.getEntity())) != null)
                        be.setPlayerIsBreaking(tiny);
                }
        }
    }

    @Override
    protected float getDestroyProgress(@NotNull BlockState state, @NotNull Player player, @NotNull BlockGetter level, @NotNull BlockPos pos)
    {
        //Return different progress because SubBlock has different Blocks inside-it with different DestroyProgress
        if (level.getBlockEntity(pos) instanceof SubBlockBE be && be.getPlayerIsBreaking() != null)
        {
            TinyBlockState tiny = be.getPlayerIsBreaking();
            float f = tiny.definition.getDestroySpeed(tiny, level, pos);
            if (f == -1.0F) {
                return 0.0F;
            } else {
                int i = net.neoforged.neoforge.event.EventHooks.doPlayerHarvestCheck(player, state, level, pos) ? 30 : 100;
                return tiny.definition.getPlayerDestroySpeed(player,tiny, level, pos) / f / (float)i;
            }
        }
        else
            return 0;
    }

    @Override
    public boolean onDestroyedByPlayer(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, boolean willHarvest, @NotNull FluidState fluid)
    {
        //break a TinyBloc inside the SubBlock instead of break the entire block.
        if (level.getBlockEntity(pos) instanceof SubBlockBE be && be.getPlayerIsBreaking() != null)
        {
            TinyBlockState tiny = be.getPlayerIsBreaking();

            boolean shouldbeRemoved = be.breakTinyState(tiny, player, willHarvest, fluid);
            //continues the pipeline if the block should be removed
            //this only happens if the BE don't have any TinyState left
            if (shouldbeRemoved)
                super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);

            return shouldbeRemoved;
        }

        return false;
    }
}
