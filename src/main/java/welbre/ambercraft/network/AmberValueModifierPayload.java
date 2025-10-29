package welbre.ambercraft.network;

import io.netty.buffer.ByteBuf;
import kuse.welbre.sim.electrical.elements.Capacitor;
import kuse.welbre.sim.electrical.elements.Inductor;
import kuse.welbre.sim.electrical.elements.Resistor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.electrical.DirectionalElectricalBE;
import welbre.ambercraft.blockentity.electrical.ElectricalBE;
import welbre.ambercraft.blocks.electrical.ResistorBlock;

import java.util.UUID;

public record AmberValueModifierPayload(UUID key, Type elementType, double value) implements CustomPacketPayload
{
    public AmberValueModifierPayload(UUID key, byte vsType, double value) {
        this(key, Type.values()[vsType], value);
    }

    public enum Type
    {
        RESISTANCE,
        CAPACITANCE,
        INDUCTANCE
    }

    public void handleOnServer(IPayloadContext context)
    {
        //first check the argumentation.
        if (key == null || elementType == null || value <= 0)
            return;
        //check the secure key
        BlockPos pos = UpdateAmberSecureKeyPayload.validadeKey(context.player().getName().getString(), DirectionalElectricalBE.class, context.player().level());
        if (pos == null)
            return;

        Level level = context.player().level();

        if (level.getBlockEntity(pos) instanceof ElectricalBE be)
        {
            switch (elementType())
            {
                case Type.RESISTANCE ->
                {
                    if (be.getElement() instanceof Resistor resistor)
                        resistor.setResistance(value);
                }
                case Type.CAPACITANCE ->
                {
                    if (be.getElement() instanceof Capacitor capacitor)
                        capacitor.setCapacitance(value);
                }
                case Type.INDUCTANCE ->
                {
                    if (be.getElement() instanceof Inductor inductor)
                        inductor.setInductance(value);
                }
                default -> throw new IllegalStateException("Unexpected value: " + type());
            }
            //dirt the network to recompile the circuit matrix with news values.
            be.getElectricalModule().dirtMaster();
        }
    }

    public static final StreamCodec<ByteBuf, AmberValueModifierPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, AmberValueModifierPayload::key,
            ByteBufCodecs.BYTE, AmberValueModifierPayload::typeAsByte,
            ByteBufCodecs.DOUBLE, AmberValueModifierPayload::value,
            AmberValueModifierPayload::new
    );

    private byte typeAsByte()
    {
        return (byte) elementType.ordinal();
    }

    public static final CustomPacketPayload.Type<AmberValueModifierPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AmberCraft.MOD_ID, "amber_value_modifier_payload"));

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
