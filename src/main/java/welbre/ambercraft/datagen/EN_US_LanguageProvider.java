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
        addBlock(AmberCraft.Blocks.HEAT_SOURCE_BLOCK, "Heat source");
        addBlock(AmberCraft.Blocks.HEAT_SINK_BLOCK, "Heat sink");

        addBlock(AmberCraft.Blocks.ABSTRACT_FACED_CABLE_BLOCK, "Abstract cable");

        addItem(AmberCraft.Items.MULTIMETER, "Multimeter");
        addItem(AmberCraft.Items.FACED_CABLE_BLOCK_ITEM, "Faced cable");
    }
}
