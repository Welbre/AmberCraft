package welbre.ambercraft.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import welbre.ambercraft.Main;

public class AmberLanguageProvider extends LanguageProvider {
    public AmberLanguageProvider(PackOutput output) {
        super(output, Main.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addBlock(Main.Blocks.IRON_MACHINE_CASE_BLOCK, "Iron machine case");
        addBlock(Main.Blocks.VOLTAGE_SOURCE_BLOCK, "Voltage source");
        addBlock(Main.Blocks.RESISTOR_BLOCK, "Resistor");
        addBlock(Main.Blocks.GROUND_BLOCK, "Ground");
        addBlock(Main.Blocks.HEAT_FURNACE_BLOCK, "Heat furnace");
        addBlock(Main.Blocks.CREATIVE_HEAT_FURNACE_BLOCK, "Creative heat furnace");
        addBlock(Main.Blocks.COPPER_HEAT_CONDUCTOR_BLOCK, "Copper heat conductor");
        addBlock(Main.Blocks.IRON_HEAT_CONDUCTOR_BLOCK, "Iron heat conductor");
        addBlock(Main.Blocks.GOLD_HEAT_CONDUCTOR_BLOCK, "Gold heat conductor");
        addBlock(Main.Blocks.HEAT_SINK_BLOCK, "Heat sink");

        addBlock(Main.Blocks.ABSTRACT_FACED_CABLE_BLOCK, "Abstract cable");

        addItem(Main.Items.MULTIMETER, "Multimeter");
        addItem(Main.Items.FACED_CABLE_BLOCK_ITEM, "Faced cable");
    }
}
