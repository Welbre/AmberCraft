package welbre.ambercraft.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
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
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.FacedCableBE;
import welbre.ambercraft.blocks.FacedCableBlock;
import welbre.ambercraft.cables.FacedCableComponent;
import welbre.ambercraft.network.facedcable.FacedCableStateChangePayload;

public class FacedCableBlockItem extends BlockItem {
    public FacedCableBlockItem(Item.Properties properties) {
        super(AmberCraft.Blocks.ABSTRACT_FACED_CABLE_BLOCK.get(),properties);
    }

    //client and server side.
    @Override
    public @NotNull InteractionResult place(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity be = level.getBlockEntity(pos);
        Direction clickedFace = context.getClickedFace();

        FacedCableComponent component = context.getItemInHand().getComponents().get(AmberCraft.Components.CABLE_DATA_COMPONENT.get());
        if (component == null)
            return InteractionResult.FAIL;

        //add the cable in a block face
        if (be instanceof FacedCableBE cable){
            if (cable.getState().getFaceStatus(clickedFace.getOpposite()) == null) {
                ItemStack item = context.getItemInHand();
                Block block = getBlock();
                BlockState state = block.defaultBlockState();
                Player player = context.getPlayer();

                cable.applyComponentsFromItemStack(item);
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

                cable.addCenter(clickedFace.getOpposite(),component);

                if (level instanceof ServerLevel serverLevel)
                {
                    FacedCableBE.UpdateShapeResult result = cable.updateState();

                    PacketDistributor.sendToPlayersInDimension(serverLevel, new FacedCableStateChangePayload(cable));
                    level.updateNeighborsAt(pos, AmberCraft.Blocks.ABSTRACT_FACED_CABLE_BLOCK.get());
                    for (var p : result.diagonal())
                        serverLevel.neighborChanged(p, AmberCraft.Blocks.ABSTRACT_FACED_CABLE_BLOCK.get(), null);

                    cable.updateBrain();
                    level.sendBlockUpdated(pos, state, state, 0);
                }

                cable.setChanged();//server save data

                return InteractionResult.SUCCESS;
            }
        }

        //at this point a cable doesn't exist where the player has clicked, so we create one.
        InteractionResult result = super.place(context);
        //the Block#onPlace is called, but it does nothing because we need to add the center in the BlockEntity, and update the shape again.
        if (result.consumesAction())
            if (level.getBlockEntity(pos) instanceof FacedCableBE faced)
                faced.addCenter(clickedFace.getOpposite(),component);

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