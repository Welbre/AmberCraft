package welbre.ambercraft.network;

import io.netty.buffer.ByteBuf;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.elements.ACVoltageSource;
import kuse.welbre.sim.electrical.elements.SquareVoltageSource;
import kuse.welbre.sim.electrical.elements.VoltageSource;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.electrical.DirectionalElectricalBE;
import welbre.ambercraft.blockentity.electrical.ElectricalBE;
import welbre.ambercraft.blocks.electrical.DirectionalElectricalBlock;

import java.util.UUID;

public record VoltageSourceModifierPayload(UUID key, VoltageSourceType vsType, double voltage, double frequency) implements CustomPacketPayload {

    public VoltageSourceModifierPayload(String key, int vsType, double voltage, double frequency)
    {
        this(UUID.fromString(key), VoltageSourceType.values()[vsType], voltage, frequency);
    }

    public void handleOnServer(IPayloadContext context) {
        //first check the argumentation.
        if (key == null || vsType == null)
            return;
        //check the secure key
        BlockPos pos = UpdateAmberSecureKeyPayload.validadeKey(context.player().getName().getString(), DirectionalElectricalBE.class, context.player().level());
        if (pos == null)
            return;

        Level level = context.player().level();
        if (level == null)
            return;

        BlockState state = switch (vsType)
        {
            case DC -> AmberCraft.Blocks.VOLTAGE_SOURCE_BLOCK.get().defaultBlockState();
            case SINE -> AmberCraft.Blocks.AC_VOLTAGE_SOURCE_BLOCK.get().defaultBlockState();
            case SQUARE -> AmberCraft.Blocks.SQUARE_VOLTAGE_SOURCE_BLOCK.get().defaultBlockState();
        };
        Direction facing = level.getBlockState(pos).getValue(DirectionalElectricalBlock.FACING);
        state = state.setValue(DirectionalElectricalBlock.FACING, facing);
        level.setBlockAndUpdate(pos, state);

        if (! (level.getBlockEntity(pos) instanceof ElectricalBE electricalBE))
            return;

        Element element = electricalBE.getElement();
        switch (vsType)
        {
            case DC -> {
                if (element instanceof VoltageSource vs)
                    vs.setSourceVoltage(voltage);
            }
            case SINE -> {
                if (element instanceof ACVoltageSource vs)
                {
                    vs.setSourceVoltage(voltage);
                    vs.setFrequency(frequency);
                }
            }
            case SQUARE -> {
                if (element instanceof SquareVoltageSource vs)
                {
                    vs.setVoltage(voltage);
                    vs.setFrequency(frequency);
                }
            }
        }
        electricalBE.setChanged();
        electricalBE.getElectricalModule().dirtMaster();
    }


    public static final StreamCodec<ByteBuf, VoltageSourceModifierPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, VoltageSourceModifierPayload::keyAsString,
            ByteBufCodecs.BYTE, VoltageSourceModifierPayload::vsTypeAsByte,
            ByteBufCodecs.DOUBLE, VoltageSourceModifierPayload::voltage,
            ByteBufCodecs.DOUBLE, VoltageSourceModifierPayload::frequency,
            VoltageSourceModifierPayload::new
            );
    public static final Type<VoltageSourceModifierPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AmberCraft.MOD_ID, "voltage_source_modifier_payload"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public String keyAsString() {
        return key.toString();
    }

    public byte vsTypeAsByte()
    {
        return (byte) vsType.ordinal();
    }

    public enum VoltageSourceType
    {
        DC,
        SINE,
        SQUARE;

        public static int getType(Element e)
        {
            return switch (e)
            {
                case ACVoltageSource ignored -> 1;
                case VoltageSource ignored -> 0;
                case SquareVoltageSource ignored -> 2;
                case null, default -> throw new IllegalArgumentException("Element isn't a voltage source!");
            };
        }
    }
}
