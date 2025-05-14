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
        addItem(Main.Items.MULTIMETER, "Multimeter");
    }
}
