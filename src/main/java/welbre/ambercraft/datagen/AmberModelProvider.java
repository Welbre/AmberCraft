package welbre.ambercraft.datagen;

import com.google.gson.JsonObject;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.*;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.Main;

import java.util.HashMap;

import static welbre.ambercraft.datagen.template.AmberModelTemplate.*;

public class AmberModelProvider extends ModelProvider {

    public AmberModelProvider(PackOutput output) {
        super(output, Main.MOD_ID);
    }

    @Override
    protected void registerModels(@NotNull BlockModelGenerators blockModels, @NotNull ItemModelGenerators itemModels) {
        registerTemplates(blockModels);
        registerItems(itemModels);
        registerBlocks(blockModels);
    }

    private static void registerTemplates(@NotNull BlockModelGenerators blockModels) {
        {
            var loc = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "block/sided_block");
            JsonObject temp = SIDED_BLOCK_TEMPLATE.createBaseTemplate(loc, new HashMap<>());
            blockModels.modelOutput.accept(loc, () -> temp);
        }
    }

    private static void registerItems(@NotNull ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(Main.Items.MULTIMETER.get(), ModelTemplates.FLAT_ITEM);
    }

    private static void registerBlocks(@NotNull BlockModelGenerators blocks) {
        blocks.createTrivialCube(Main.Blocks.IRON_MACHINE_CASE_BLOCK.get());
        blocks.createTrivialCube(Main.Blocks.COPPER_HEAT_CONDUCTOR_BLOCK.get());
        blocks.createTrivialCube(Main.Blocks.IRON_HEAT_CONDUCTOR_BLOCK.get());

        CREATE_AMBER_SIDED_BLOCK(blocks, Main.Blocks.VOLTAGE_SOURCE_BLOCK.get(),
                "connection_creative_block", "connection_creative_block", "voltage_source_block");
        CREATE_AMBER_SIDED_BLOCK(blocks, Main.Blocks.RESISTOR_BLOCK.get(),
                "connection_creative_block", "connection_creative_block", "resistor_block");
        CREATE_AMBER_HORIZONTAL_BLOCK(blocks, Main.Blocks.HEAT_FURNACE_BLOCK.get(),
                "brick_machine_block", "furnace_base_block","iron_machine_base_beauty_block");
        CREATE_AMBER_SIDED_BLOCK(blocks, Main.Blocks.GROUND_BLOCK.get(),
                "connection_creative_block", "ground_block","ground_block");

        CREATE_AMBER_FREE_BLOCK_STATE(blocks, Main.Blocks.CREATIVE_HEAT_FURNACE_BLOCK.get());
    }
}
