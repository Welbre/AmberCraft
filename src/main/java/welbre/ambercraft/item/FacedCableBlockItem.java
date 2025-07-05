package welbre.ambercraft.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.Main;
import welbre.ambercraft.blockentity.FacedCableBlockEntity;
import welbre.ambercraft.blocks.FacedCableBlock;
import welbre.ambercraft.cables.AmberFCableComponent;

public class FacedCableBlockItem extends BlockItem {
    public FacedCableBlockItem(Item.Properties properties) {
        super(Main.Blocks.ABSTRACT_FACED_CABLE_BLOCK.get(),properties);
    }
    @Override
    public @NotNull InteractionResult place(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity be = level.getBlockEntity(pos);
        Direction clickedFace = context.getClickedFace();

        AmberFCableComponent component = context.getItemInHand().getComponents().get(Main.Components.CABLE_DATA_COMPONENT.get());
        if (component == null)
            return InteractionResult.FAIL;


        if (be instanceof FacedCableBlockEntity faced){
            if (faced.getState().getFaceStatus(clickedFace.getOpposite()) == null) {
                ItemStack item = context.getItemInHand();
                Block block = getBlock();
                BlockState state = block.defaultBlockState();
                Player player = context.getPlayer();

                faced.applyComponentsFromItemStack(item);
                block.setPlacedBy(level,pos, state, player, item);
                if (player instanceof ServerPlayer)
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player,pos, item);

                SoundType soundtype = state.getSoundType(level, pos, player);
                level.playSound(
                        player,
                        pos,
                        this.getPlaceSound(state, level, pos, player),
                        SoundSource.BLOCKS,
                        (soundtype.getVolume() + 1.0F) / 2.0F,
                        soundtype.getPitch() * 0.8F
                );
                level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, state));
                item.consume(1, player);

                faced.addCenter(clickedFace.getOpposite(),component);
                faced.calculateState(level,pos);
                faced.requestModelDataUpdate();
                faced.setChanged();
                level.markAndNotifyBlock(pos,level.getChunkAt(pos),level.getBlockState(pos),level.getBlockState(pos),3,512);
                return InteractionResult.SUCCESS;
            }
        }
        InteractionResult result = super.place(context);
        //put the cable if don't have on the block.
        if (result.consumesAction()) {
            if (level.getBlockEntity(pos) instanceof FacedCableBlockEntity faced)
            {
                faced.addCenter(clickedFace.getOpposite(),component);
                faced.calculateState(level,pos);
                faced.requestModelDataUpdate();
                faced.setChanged();
            }
        }

        return result;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return super.useOn(context);
    }

    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        if (level.getBlockState(clicked.relative(context.getClickedFace().getOpposite())).getBlock() instanceof FacedCableBlock)
            return false;
        return super.canPlace(context, state);
    }


}
