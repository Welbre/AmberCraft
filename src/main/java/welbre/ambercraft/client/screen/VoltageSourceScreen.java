package welbre.ambercraft.client.screen;

import io.netty.buffer.Unpooled;
import kuse.welbre.sim.electrical.elements.ACVoltageSource;
import kuse.welbre.sim.electrical.elements.SquareVoltageSource;
import kuse.welbre.sim.electrical.elements.VoltageSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.electrical.ElectricalBE;
import welbre.ambercraft.network.UpdateAmberSecureKeyPayload;
import welbre.ambercraft.network.VoltageSourceModifierPayload;

import java.util.function.Predicate;

import static welbre.ambercraft.network.VoltageSourceModifierPayload.*;

public class VoltageSourceScreen extends Screen {
    /// Used to set the position in the done button when the ac mode is active.
    private static final int DONE_BUTTON_AC_Y_POSITION = 300;

    public VoltageSourceType type;

    public EditBox voltageBox;
    public double voltage;

    StringWidget frequencyString;
    public EditBox frequencyBox;
    public double frequency;

    public CycleButton<VoltageSourceType> modeButton;
    public boolean isAC;
    public Button done;

    public VoltageSourceScreen(FriendlyByteBuf buf)
    {
        super(Component.translatable("ambercraft.voltage.screen.title"));
        this.voltage = buf.readDouble();
        this.type = VoltageSourceType.values()[buf.readByte()];
        isAC = type != VoltageSourceType.DC;
        if (isAC)
            frequency = buf.readDouble();
    }

    @Override
    protected void init() {
        StringWidget voltageString;
        {
            Component text = Component.translatable("ambercraft.voltage.screen.voltage");
            int textWidth = font.width(text);
            voltageString = new StringWidget(width/2-110 - textWidth, 125, textWidth, 20, text, font);
        }
        voltageBox = new EditBox(font, width / 2 - 100, 125, 100, 20, Component.translatable("ambercraft.voltage.screen.voltage"));
        voltageBox.setResponder(this::VOLTAGE_BOX_RESPONDER);
        voltageBox.setFilter(DOUBLE_BOX_FILTER(voltageBox));
        voltageBox.setValue(String.valueOf(voltage));

        {
            Component text = Component.translatable("ambercraft.voltage.screen.frequency");
            int textWidth = font.width(text);
            frequencyString = new StringWidget(width/2-110 - textWidth, 150, textWidth, 20, text, font);
            frequencyString.visible = false;
        }
        frequencyBox = new EditBox(font, width / 2 - 100, 150, 100, 20, Component.literal("ambercraft.voltage.screen.frequency"));
        frequencyBox.setResponder(this::FREQUENCY_BOX_RESPONDER);
        frequencyBox.setFilter(DOUBLE_BOX_FILTER(frequencyBox));
        frequencyBox.setValue(String.valueOf(frequency));
        frequencyBox.visible = false;

        modeButton = CycleButton.builder(VoltageSourceScreen::CYCLE_BUTTON_TEXT)
                .withValues(VoltageSourceType.values())
                .create(width / 2 - 100, 100, 200, 20, Component.translatable("ambercraft.voltage.screen.mode"), this::MODE_CHANGED);
        done = Button.builder(Component.translatable("ambercraft.voltage.screen.done"), this::DONE_BUTTON_ON_PRESS).bounds(width / 2 - 100, 155, 30, 20).build();

        addRenderableWidget(voltageString);
        addRenderableWidget(voltageBox);
        addRenderableWidget(frequencyString);
        addRenderableWidget(frequencyBox);
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
                voltage,
                frequency
        ));
    }

    private static Predicate<String> DOUBLE_BOX_FILTER(EditBox box)
    {
        return (string) -> {
            if (string.isEmpty()) return true;

            try
            {
                Double.parseDouble(string);
                box.setTextColor(0x44cc44);
                return true;
            } catch (NumberFormatException e)
            {
                box.setTextColor(0xcc4444);
                return false;
            }
        };
    }

    private void VOLTAGE_BOX_RESPONDER(String string)
    {
        if (string.isEmpty())
        {
            voltageBox.setSuggestion(Component.translatable("ambercraft.voltage.screen.voltage_suggestion").getString());
        } else {
            voltageBox.setSuggestion("");
            voltage = Double.parseDouble(string);
        }
    }

    private void FREQUENCY_BOX_RESPONDER(String string)
    {
        if (string.isEmpty())
        {
            frequencyBox.setSuggestion(Component.translatable("ambercraft.voltage.screen.frequency_suggestion").getString());
        } else {
            frequencyBox.setSuggestion("");
            frequency = Double.parseDouble(string);
        }
    }

    private static Component CYCLE_BUTTON_TEXT(VoltageSourceType type)
    {
        return Component.translatable("ambercraft.voltage.screen.voltage_mode." + type.name().toLowerCase());
    }

    private void MODE_CHANGED(CycleButton<VoltageSourceType> checkbox, VoltageSourceType type)
    {
        this.type = type;
        isAC = type != VoltageSourceType.DC;
        frequencyString.visible = isAC;
        frequencyBox.visible = isAC;
        done.setPosition(width / 2 - 100, isAC ? DONE_BUTTON_AC_Y_POSITION : 155);
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
                buf.writeDouble(square.getFrequency());
        }

        return buf;
    }
}
