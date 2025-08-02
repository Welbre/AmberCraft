package welbre.ambercraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.HeatSourceBE;

import java.util.function.Function;

public record HeatSourceSetterPayload(BlockPos pos, double temperature, double heat, String mode) implements CustomPacketPayload {
    public static void handleOnServer(HeatSourceSetterPayload payload, IPayloadContext context)
    {
        if(!context.player().isCreative())
            return;

        if (context.player().level().getBlockEntity(payload.pos) instanceof HeatSourceBE source)
        {
            source.temperature = payload.temperature();
            source.heat = payload.heat();
            source.mode = HeatSourceBE.Mode.valueOf(payload.mode());
            source.setChanged();
        }
    }


    public static final StreamCodec<ByteBuf, HeatSourceSetterPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, HeatSourceSetterPayload::pos,
            ByteBufCodecs.DOUBLE, HeatSourceSetterPayload::temperature,
            ByteBufCodecs.DOUBLE, HeatSourceSetterPayload::heat,
            ByteBufCodecs.STRING_UTF8, HeatSourceSetterPayload::mode,
            HeatSourceSetterPayload::new);

    public HeatSourceSetterPayload(HeatSourceBE source)
    {
        this(source.getBlockPos(), source.temperature,source.heat, source.mode.name());
    }


    public static final CustomPacketPayload.Type<HeatSourceSetterPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AmberCraft.MOD_ID, "heat_source_setter_payload"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
