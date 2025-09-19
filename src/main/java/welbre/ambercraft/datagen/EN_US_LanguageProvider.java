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
        addBlock(AmberCraft.Blocks.VOLTAGE_SOURCE_BLOCK, "Voltage source");
        addBlock(AmberCraft.Blocks.RESISTOR_BLOCK, "Resistor");
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

        addBlock(AmberCraft.Blocks.ABSTRACT_FACED_CABLE_BLOCK, "Abstract cable");

        addItem(AmberCraft.Items.NETWORK_TOOL, "Network Tool");
        addItem(AmberCraft.Items.THERMOMETER, "Thermometer");
        addMultimeter();
        addItem(AmberCraft.Items.FACED_CABLE_BLOCK_ITEM, "Faced cable");
    }


    private void addMultimeter()
    {
        add("item.ambercraft.multimeter", "Multimeter");
        add("item.ambercraft.multimeter.mode_changed", "Multimeter mode changed to: %s");
        add("item.ambercraft.multimeter.too_far", "You are too far away from the module!");
        add("item.ambercraft.multimeter.voltage", "Voltage: %s");
        add("item.ambercraft.multimeter.current", "Current: %s");
        add("item.ambercraft.multimeter.power", "Power: %s");
        add("item.ambercraft.multimeter.resistence", "Resistence: %s");
        add("item.ambercraft.multimeter.first_click", "First point selected");
        add("item.ambercraft.multimeter.voltage_same_spot", "You are measuring the voltage difference in the same point, it will always be zero!");
        add("item.ambercraft.multimeter.current_same_terminal", "You can't measure the current in the same terminal!");
        add("item.ambercraft.multimeter.current_dif_element", "You can't measure the current in different element!");
        add("item.ambercraft.multimeter.current_dif_circuit", "You can't measure the current in different circuits!");
    }
}
