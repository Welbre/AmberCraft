package welbre.ambercraft.subblock.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.subblock.SubBlockBE;


public record SubBlockStartBreakingState(BlockPos pos, int x, int y, int z) implements CustomPacketPayload {

    public void handleOnServer(IPayloadContext context)
    {
        if (context.player().level().getBlockEntity(pos) instanceof SubBlockBE sub)
            sub.setPlayerIsBreaking(sub.getTinyStateByPos(x, y, z));
    }


    public static final StreamCodec<ByteBuf, SubBlockStartBreakingState> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SubBlockStartBreakingState::pos,
            ByteBufCodecs.INT, SubBlockStartBreakingState::x,
            ByteBufCodecs.INT, SubBlockStartBreakingState::y,
            ByteBufCodecs.INT, SubBlockStartBreakingState::z,
            SubBlockStartBreakingState::new);


    public static final CustomPacketPayload.Type<SubBlockStartBreakingState> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AmberCraft.MOD_ID, "sub_block_start_breaking_state"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
