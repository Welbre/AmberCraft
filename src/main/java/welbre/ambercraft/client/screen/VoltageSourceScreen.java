package welbre.ambercraft.client.screen;

import io.netty.buffer.Unpooled;
import kuse.welbre.sim.electrical.elements.ACVoltageSource;
import kuse.welbre.sim.electrical.elements.SquareVoltageSource;
import kuse.welbre.sim.electrical.elements.VoltageSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.electrical.ElectricalBE;
import welbre.ambercraft.client.screen.widget.ParameterSet;
import welbre.ambercraft.network.UpdateAmberSecureKeyPayload;
import welbre.ambercraft.network.VoltageSourceModifierPayload;

import static net.minecraft.network.chat.Component.translatable;
import static welbre.ambercraft.network.VoltageSourceModifierPayload.VoltageSourceType;

public class VoltageSourceScreen extends Screen {
    /// Used to set the position in the done button when the ac mode is active.
    private static final int DONE_BUTTON_AC_Y_POSITION = 175;
    private static final int DONE_BUTTON_SQUARE_Y_POSITION = 250;

    public VoltageSourceType type;
    public double initialVoltage;
    public double initialFrequency;
    public double initialDutyCycle;
    public double initialPhaseShift;
    public double initialVOff;

    public ParameterSet voltageSet;
    public ParameterSet frequencySet;
    public ParameterSet dutyCycleSet;
    public ParameterSet phaseShiftSet;
    public ParameterSet vOffSet;

    public CycleButton<VoltageSourceType> modeButton;
    public boolean isAC;
    public boolean isSquare;
    public Button done;

    public VoltageSourceScreen(FriendlyByteBuf buf)
    {
        super(translatable("ambercraft.voltage.screen.title"));
        this.initialVoltage = buf.readDouble();
        this.type = VoltageSourceType.values()[buf.readByte()];
        isAC = type != VoltageSourceType.DC;
        isSquare = type == VoltageSourceType.SQUARE;
        this.initialFrequency = isAC ? buf.readDouble() : 1;
        this.initialDutyCycle =  (isSquare ? buf.readDouble() : 0.5) / initialFrequency;
        this.initialPhaseShift = isSquare ? buf.readDouble() : 0;
        this.initialVOff = isSquare ? buf.readDouble() : 0;
    }

    @Override
    protected void init() {
        voltageSet = new ParameterSet(this,
                width / 2 - 100, 125, 100, 20,
                translatable("ambercraft.voltage.screen.voltage"), translatable("ambercraft.voltage.screen.voltage_suggestion"), font,
                ParameterSet.interpolation::linear, initialVoltage, 1000, -1000);

        frequencySet = new ParameterSet(this,
                width / 2 -100, 150, 100, 20,
                translatable("ambercraft.voltage.screen.frequency"), translatable("ambercraft.voltage.screen.voltage_suggestion"), font,
                (v) -> Math.pow(v, 2.0), initialFrequency, 120, 0.05);
        dutyCycleSet = new ParameterSet(this,
                width / 2 -100, 175, 100, 20,
                translatable("ambercraft.voltage.screen.dutycycle"), translatable("ambercraft.voltage.screen.dutycycle_suggestion"), font,
                ParameterSet.interpolation::linear, initialDutyCycle, 1, 0);
        phaseShiftSet = new ParameterSet(this,
                width / 2 -100, 200, 100, 20,
                translatable("ambercraft.voltage.screen.phaseshift"), translatable("ambercraft.voltage.screen.phaseshift_suggestion"), font,
                ParameterSet.interpolation::linear, initialPhaseShift, 360, 0);
        vOffSet = new ParameterSet(this,
                width / 2 -100, 225, 100, 20,
                translatable("ambercraft.voltage.screen.voff"), translatable("ambercraft.voltage.screen.voff_suggestion"), font,
                ParameterSet.interpolation::linear, initialVOff, 500, -500);


        modeButton = CycleButton.builder(VoltageSourceScreen::CYCLE_BUTTON_TEXT)
                .withValues(VoltageSourceType.values())
                .create(width / 2 - 100, 100, 205, 20, translatable("ambercraft.voltage.screen.mode"), this::MODE_CHANGED);
        done = Button.builder(translatable("ambercraft.voltage.screen.done"), this::DONE_BUTTON_ON_PRESS).bounds(width / 2 - 100, 155, 30, 20).build();

        addRenderableWidget(modeButton);
        addRenderableWidget(done);

        MODE_CHANGED(modeButton, type);//update the components to the actual type
    }

    private void DONE_BUTTON_ON_PRESS(Button button)
    {
        Minecraft.getInstance().setScreen(null);
        PacketDistributor.sendToServer(new VoltageSourceModifierPayload(
                UpdateAmberSecureKeyPayload.CLIENT_KEY,
                type,
                voltageSet.value,
                frequencySet.value,
                dutyCycleSet.value,
                Math.toRadians(phaseShiftSet.value),
                vOffSet.value
        ));
    }

    private static Component CYCLE_BUTTON_TEXT(VoltageSourceType type)
    {
        return translatable("ambercraft.voltage.screen.voltage_mode." + type.name().toLowerCase());
    }

    private void MODE_CHANGED(CycleButton<VoltageSourceType> checkbox, VoltageSourceType type)
    {
        this.type = type;
        isAC = type != VoltageSourceType.DC;
        isSquare = type == VoltageSourceType.SQUARE;

        frequencySet.setVisible(isAC);
        dutyCycleSet.setVisible(isSquare);
        phaseShiftSet.setVisible(isSquare);
        vOffSet.setVisible(isSquare);

        done.setPosition(width / 2 - 100, isSquare ? DONE_BUTTON_SQUARE_Y_POSITION : isAC ? DONE_BUTTON_AC_Y_POSITION : 150);
        modeButton.setValue(type);
    }

    public static FriendlyByteBuf CREATE_BUFFER(Level level, BlockPos pos)
    {
        if (level == null) throw new IllegalArgumentException("Level can't be null!");
        if (pos == null) throw new IllegalArgumentException("Position can't be null!");
        if (!
                (
                level.getBlockState(pos).is(AmberCraft.Blocks.VOLTAGE_SOURCE_BLOCK) ||
                level.getBlockState(pos).is(AmberCraft.Blocks.AC_VOLTAGE_SOURCE_BLOCK) ||
                level.getBlockState(pos).is(AmberCraft.Blocks.SQUARE_VOLTAGE_SOURCE_BLOCK)
                )
            )
            throw new IllegalArgumentException("Block in the position must be a voltage source!");
        if (!(level.getBlockEntity(pos) instanceof ElectricalBE source)) throw new IllegalArgumentException("BlockEntity in the position must be an electrical block entity!");
        int type = VoltageSourceType.getType(source.getElement());

        var buf = new FriendlyByteBuf(Unpooled.buffer());
        double voltage = 0;
        if (type == 0)
        {
            if (source.getElement() instanceof VoltageSource vs)
                voltage = vs.getVoltageDifference();
        } else if (type == 1)
        {
            if (source.getElement() instanceof ACVoltageSource vs)
                voltage = vs.getSourceVoltage();
        } else if (type == 2)
        {
            if (source.getElement() instanceof SquareVoltageSource sq)
                voltage = sq.getSourceVoltage();
        }
        buf.writeDouble(voltage);
        buf.writeByte((byte) type);
        if (type != 0)//isn't dc
        {
            if (source.getElement() instanceof ACVoltageSource acVoltageSource)
                buf.writeDouble(acVoltageSource.getFrequency());
            else if (source.getElement() instanceof SquareVoltageSource square)
            {
                buf.writeDouble(square.getFrequency());
                buf.writeDouble(square.getDutyCycle());
                buf.writeDouble(square.getPhaseShift());
                buf.writeDouble(square.getV_off());
            }
        }

        return buf;
    }
}
