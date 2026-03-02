package welbre.ambercraft.mixins.subblock;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.subblock.SubBlockBE;

import java.util.Objects;

//@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin
{

    @Shadow protected ServerLevel level;
    @Final @Shadow protected ServerPlayer player;
    @Shadow private GameType gameModeForPlayer;
    @Shadow private int destroyProgressStart;
    @Shadow private int delayedTickStart;
    @Shadow private int gameTicks;
    @Shadow private int lastSentState;
    @Shadow private boolean isDestroyingBlock;
    @Shadow private boolean hasDelayedDestroy;
    @Shadow private BlockPos destroyPos;
    @Shadow private BlockPos delayedDestroyPos;

    @Shadow protected abstract void debugLogging(BlockPos pos, boolean success, int sequence, String reason);
    @Shadow public abstract void destroyAndAck(BlockPos pos, int sequence, String message);
    @Shadow public abstract boolean isCreative();
    @Shadow protected abstract boolean removeBlock(BlockPos pos, BlockState state, boolean canHarvest);


    //@Inject(method = "handleBlockBreakAction", at=@At("HEAD"), cancellable = true)
    public void onHandleBreakAction(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction face, int maxBuildHeight, int sequence, CallbackInfo ci)
    {
        if (true)
            return;
        //Check if block is SubBlock
        SubBlockBE sub = null;
        if (level.getBlockEntity(pos) instanceof SubBlockBE be)
            sub = be;
        if (sub == null)
            return;

        ci.cancel();
        //Rewrite the default minecraft pipe-line to the sub block.
        net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event = net.neoforged.neoforge.common.CommonHooks.onLeftClickBlock(player, pos, face, action);
        if (event.isCanceled())
            return;
        if (!this.player.canInteractWithBlock(pos, 1.0)) {
            this.debugLogging(pos, false, sequence, "too far");
        } else if (pos.getY() > maxBuildHeight) {
            this.player.connection.send(new ClientboundBlockUpdatePacket(pos, this.level.getBlockState(pos)));
            this.debugLogging(pos, false, sequence, "too high");
        } else
        {
            if (action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK)
            {
                if (!this.level.mayInteract(this.player, pos)) {
                    this.player.connection.send(new ClientboundBlockUpdatePacket(pos, this.level.getBlockState(pos)));
                    this.debugLogging(pos, false, sequence, "may not interact");
                    return;
                }

                if (this.isCreative()) {
                    this.destroyAndAck(pos, sequence, "creative destroy");
                    return;
                }

                if (this.player.blockActionRestricted(this.level, pos, this.gameModeForPlayer)) {
                    this.player.connection.send(new ClientboundBlockUpdatePacket(pos, this.level.getBlockState(pos)));
                    this.debugLogging(pos, false, sequence, "block action restricted");
                    return;
                }

                this.destroyProgressStart = this.gameTicks;
                float f = 1.0F;
                BlockState blockstate = this.level.getBlockState(pos);
                if (!blockstate.isAir()) {
                    EnchantmentHelper.onHitBlock(
                            this.level,
                            this.player.getMainHandItem(),
                            this.player,
                            this.player,
                            EquipmentSlot.MAINHAND,
                            Vec3.atCenterOf(pos),
                            blockstate,
                            p_348149_ -> this.player.onEquippedItemBroken(p_348149_, EquipmentSlot.MAINHAND)
                    );
                    if (event.getUseBlock() != net.neoforged.neoforge.common.util.TriState.FALSE)
                        blockstate.attack(this.level, pos, this.player);
                    f = blockstate.getDestroyProgress(this.player, this.player.level(), pos);
                }

                if (!blockstate.isAir() && f >= 1.0F) {
                    this.destroyAndAck(pos, sequence, "insta mine");
                } else {
                    if (this.isDestroyingBlock) {
                        this.player.connection.send(new ClientboundBlockUpdatePacket(this.destroyPos, this.level.getBlockState(this.destroyPos)));
                        this.debugLogging(pos, false, sequence, "abort destroying since another started (client insta mine, server disagreed)");
                    }

                    this.isDestroyingBlock = true;
                    this.destroyPos = pos.immutable();
                    int i = (int)(f * 10.0F);
                    this.level.destroyBlockProgress(this.player.getId(), pos, i);
                    this.debugLogging(pos, true, sequence, "actual start of destroying");
                    this.lastSentState = i;
                }
            }
            else if (action == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK)
            {
                if (pos.equals(this.destroyPos)) {
                    int j = this.gameTicks - this.destroyProgressStart;
                    BlockState blockstate1 = this.level.getBlockState(pos);
                    if (!blockstate1.isAir()) {
                        float f1 = blockstate1.getDestroyProgress(this.player, this.player.level(), pos) * (float)(j + 1);
                        if (f1 >= 0.7F) {
                            this.isDestroyingBlock = false;
                            this.level.destroyBlockProgress(this.player.getId(), pos, -1);
                            this.destroyAndAck(pos, sequence, "destroyed");
                            return;
                        }

                        if (!this.hasDelayedDestroy) {
                            this.isDestroyingBlock = false;
                            this.hasDelayedDestroy = true;
                            this.delayedDestroyPos = pos;
                            this.delayedTickStart = this.destroyProgressStart;
                        }
                    }
                }

                this.debugLogging(pos, true, sequence, "stopped destroying");
            }
            else if (action == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK)
            {
                this.isDestroyingBlock = false;
                if (!Objects.equals(this.destroyPos, pos)) {
                    AmberCraft.LOGGER.warn("Mismatch in destroy block pos: {} {}", this.destroyPos, pos);
                    this.level.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
                    this.debugLogging(pos, true, sequence, "aborted mismatched destroying");
                }

                this.level.destroyBlockProgress(this.player.getId(), pos, -1);
                this.debugLogging(pos, true, sequence, "aborted destroying");
            }
        }

    }

    //@Inject(method = "destroyBlock", at=@At("HEAD"), cancellable = true)
    public void destroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir)
    {
        BlockState state = this.level.getBlockState(pos);
        var event = net.neoforged.neoforge.common.CommonHooks.fireBlockBreak(level, gameModeForPlayer, player, pos, state);
        if (event.isCanceled()) {
            cir.setReturnValue(false);
            return;
        } else {
            BlockEntity blockentity = this.level.getBlockEntity(pos);
            Block block = state.getBlock();
            if (block instanceof GameMasterBlock && !this.player.canUseGameMasterBlocks()) {
                this.level.sendBlockUpdated(pos, state, state, 3);
                cir.setReturnValue(false);
                return;
            } else if (this.player.blockActionRestricted(this.level, pos, this.gameModeForPlayer)) {
                cir.setReturnValue(false);
                return;
            } else {
                BlockState blockstate = block.playerWillDestroy(this.level, pos, state, this.player);

                if (this.isCreative()) {
                    removeBlock(pos, blockstate, false);
                    //return true;
                } else {
                    ItemStack itemstack = this.player.getMainHandItem();
                    ItemStack itemstack1 = itemstack.copy();
                    boolean flag1 = blockstate.canHarvestBlock(this.level, pos, this.player); // previously player.hasCorrectToolForDrops(blockstate)
                    itemstack.mineBlock(this.level, blockstate, pos, this.player);
                    boolean flag = removeBlock(pos, blockstate, flag1);

                    if (flag1 && flag) {
                        block.playerDestroy(this.level, this.player, pos, blockstate, blockentity, itemstack1);
                    }

                    // Neo: Fire the PlayerDestroyItemEvent if the tool was broken at any point during the break process
                    if (itemstack.isEmpty() && !itemstack1.isEmpty()) {
                        net.neoforged.neoforge.event.EventHooks.onPlayerDestroyItem(this.player, itemstack1, InteractionHand.MAIN_HAND);
                    }

                    //return true;
                }
                cir.setReturnValue(true);
                return;
            }
        }
    }
}
