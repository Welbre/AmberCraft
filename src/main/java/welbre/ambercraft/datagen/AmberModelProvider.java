package welbre.ambercraft.datagen;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.Main;

public class AmberModelProvider extends ModelProvider {
    public AmberModelProvider(PackOutput output) {
        super(output, Main.MOD_ID);
    }

    @Override
    protected void registerModels(@NotNull BlockModelGenerators blockModels, @NotNull ItemModelGenerators itemModels) {
        super.registerModels(blockModels, itemModels);
    }
}
