package welbre.ambercraft.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import welbre.ambercraft.AmberCraft;

public class EN_US_LanguageProvider extends LanguageProvider {
    public EN_US_LanguageProvider(PackOutput output) {
        super(output, AmberCraft.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addBlock(AmberCraft.Blocks.IRON_MACHINE_CASE_BLOCK, "Iron machine case");

        //electrical
        addBlock(AmberCraft.Blocks.VOLTAGE_SOURCE_BLOCK, "Voltage source");
        addBlock(AmberCraft.Blocks.AC_VOLTAGE_SOURCE_BLOCK, "Alternated voltage source");
        addBlock(AmberCraft.Blocks.SQUARE_VOLTAGE_SOURCE_BLOCK, "Square voltage source");
        addVoltageScreen();
        addBlock(AmberCraft.Blocks.RESISTOR_BLOCK, "Resistor");
        addBlock(AmberCraft.Blocks.CAPACITOR_BLOCK, "Capacitor");
        add("ambercraft.capacitance.set", "Capacitance set to: %s");
        addBlock(AmberCraft.Blocks.INDUCTOR_BLOCK, "Inductor");
        add("ambercraft.inductance.set", "Inductance set to: %s");
        addBlock(AmberCraft.Blocks.GROUND_BLOCK, "Ground");

        addBlock(AmberCraft.Blocks.HEAT_FURNACE_BLOCK, "Heat furnace");
        addBlock(AmberCraft.Blocks.CREATIVE_HEAT_FURNACE_BLOCK, "Creative heat furnace");
        addBlock(AmberCraft.Blocks.COPPER_HEAT_CONDUCTOR_BLOCK, "Copper heat conductor");
        addBlock(AmberCraft.Blocks.IRON_HEAT_CONDUCTOR_BLOCK, "Iron heat conductor");
        addBlock(AmberCraft.Blocks.GOLD_HEAT_CONDUCTOR_BLOCK, "Gold heat conductor");
        addBlock(AmberCraft.Blocks.CREATIVE_HEAT_CONDUCTOR_BLOCK, "Creative heat conductor");
        addBlock(AmberCraft.Blocks.HEAT_SINK_BLOCK, "Heat sink");
        addBlock(AmberCraft.Blocks.HEAT_SOURCE_BLOCK, "Heat source");
        addBlock(AmberCraft.Blocks.HEAT_PUMP_BLOCK, "Heat pump");

        addBlock(AmberCraft.Blocks.FACED_CABLE_BLOCK, "Abstract cable");

        //electrical measures
        addElectricalMeasures();

        //tools
        addItem(AmberCraft.Items.NETWORK_TOOL, "Network Tool");
        addItem(AmberCraft.Items.THERMOMETER, "Thermometer");
        addMultimeter();
        addItem(AmberCraft.Items.FACED_CABLE_BLOCK_ITEM, "Faced cable");
    }


    private void addVoltageScreen()
    {
        add("ambercraft.voltage.set", "Voltage set to: %s");
        add("ambercraft.voltage.screen.title", "Voltage source settings");
        add("ambercraft.voltage.screen.done", "Done");
        add("ambercraft.voltage.screen.voltage", "Voltage");
        add("ambercraft.voltage.screen.frequency", "Frequency");
        add("ambercraft.voltage.screen.dutycycle", "Duty cycle");
        add("ambercraft.voltage.screen.phaseshift", "Phase shift");
        add("ambercraft.voltage.screen.voff", "Voltage offset");
        add("ambercraft.voltage.screen.voltage_suggestion", "The voltage difference between the terminals in Volts");
        add("ambercraft.voltage.screen.frequency_suggestion", "The source frequency in Hertz");
        add("ambercraft.voltage.screen.dutycycle_suggestion", "The ratio between the \"on\" state and the \"off\" on a period in percent");
        add("ambercraft.voltage.screen.phaseshift_suggestion", "The fase dislocation in degrees");
        add("ambercraft.voltage.screen.voff_suggestion", "A fix voltage component on the source in Volts");
        add("ambercraft.voltage.screen.mode", "Mode");
        add("ambercraft.voltage.screen.voltage_mode.dc", "Direct voltage");
        add("ambercraft.voltage.screen.voltage_mode.sine", "Alternated voltage");
        add("ambercraft.voltage.screen.voltage_mode.square", "Square voltage");
    }

    private void addMultimeter()
    {
        add("item.ambercraft.multimeter", "Multimeter");
        add("item.ambercraft.multimeter.mode_changed", "Multimeter mode changed to: %s");
        add("item.ambercraft.multimeter.too_far", "You are too far away from the module!");
        add("item.ambercraft.multimeter.voltage", "Voltage: %s");
        add("item.ambercraft.multimeter.current", "Current: %s");
        add("item.ambercraft.multimeter.power", "Power: %s");
        add("item.ambercraft.multimeter.resistance", "Resistance: %s");
        add("item.ambercraft.multimeter.first_click", "First point selected");
        add("item.ambercraft.multimeter.voltage_same_spot", "You are measuring the voltage difference in the same point, it will always be zero!");
        add("item.ambercraft.multimeter.current_same_terminal", "You can't measure the current in the same terminal!");
        add("item.ambercraft.multimeter.current_dif_element", "You can't measure the current in different element!");
        add("item.ambercraft.multimeter.current_dif_circuit", "You can't measure the current in different circuits!");
    }

    private void addElectricalMeasures()
    {
        add("ambercraft.measures.voltage", "Voltage");
        add("ambercraft.measures.current", "Current");
        add("ambercraft.measures.resistance", "Resistance");
        add("ambercraft.measures.capacitance", "Capacitance");
        add("ambercraft.measures.inductance", "Inductance");
        add("ambercraft.measures.power", "Power");
    }
}
