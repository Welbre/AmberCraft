package welbre.ambercraft.datagen;

import com.google.gson.JsonObject;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.blockstates.Variant;
import net.minecraft.client.data.models.blockstates.VariantProperties;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.FurnaceBlock;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blocks.parent.AmberHorizontalBlock;
import welbre.ambercraft.client.item.CableSpecialRender;

import java.util.HashMap;

import static welbre.ambercraft.AmberCraft.MOD_ID;
import static welbre.ambercraft.datagen.template.AmberModelTemplate.*;

public class AmberModelProvider extends ModelProvider {

    public AmberModelProvider(PackOutput output) {
        super(output, AmberCraft.MOD_ID);
    }

    @Override
    protected void registerModels(@NotNull BlockModelGenerators blockModels, @NotNull ItemModelGenerators itemModels) {
        registerTemplates(blockModels);
        registerItems(itemModels);
        registerBlocks(blockModels);
    }

    private static void registerTemplates(@NotNull BlockModelGenerators blockModels) {
        {
            var loc = ResourceLocation.fromNamespaceAndPath(AmberCraft.MOD_ID, "block/sided_block");
            JsonObject temp = SIDED_BLOCK_TEMPLATE.createBaseTemplate(loc, new HashMap<>());
            blockModels.modelOutput.accept(loc, () -> temp);
        }
    }

    private static void registerItems(@NotNull ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(AmberCraft.Items.MULTIMETER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.itemModelOutput.accept(AmberCraft.Items.FACED_CABLE_BLOCK_ITEM.get(),new SpecialModelWrapper.Unbaked(ResourceLocation.parse("minecraft:block/white_wool"),new CableSpecialRender.UnBacked(
                ResourceLocation.withDefaultNamespace("block/blue_wool"))
        ));
    }

    private static void registerBlocks(@NotNull BlockModelGenerators blocks) {
        blocks.createTrivialCube(AmberCraft.Blocks.IRON_MACHINE_CASE_BLOCK.get());
        blocks.blockStateOutput.accept(MultiVariantGenerator.multiVariant(AmberCraft.Blocks.HEAT_SINK_BLOCK.get(), new Variant().with(VariantProperties.MODEL, ResourceLocation.parse("minecraft:block/air"))));

        CREATE_AMBER_SIDED_BLOCK(blocks, AmberCraft.Blocks.VOLTAGE_SOURCE_BLOCK.get(),
                "connection_creative_block", "connection_creative_block", "voltage_source_block");
        CREATE_AMBER_SIDED_BLOCK(blocks, AmberCraft.Blocks.RESISTOR_BLOCK.get(),
                "connection_creative_block", "connection_creative_block", "resistor_block");
        CREATE_AMBER_SIDED_BLOCK(blocks, AmberCraft.Blocks.GROUND_BLOCK.get(),
                "connection_creative_block", "ground_block","ground_block");

        CREATE_HEAT_FURNACE(blocks);
        CREATE_AMBER_FREE_BLOCK_STATE(blocks, AmberCraft.Blocks.CREATIVE_HEAT_FURNACE_BLOCK.get());

        CABLES.CREATE_CENTRED(blocks, AmberCraft.Blocks.COPPER_HEAT_CONDUCTOR_BLOCK.get(), ResourceLocation.parse("ambercraft:block/copper_heat_conductor"));
        CABLES.CREATE_CENTRED(blocks, AmberCraft.Blocks.IRON_HEAT_CONDUCTOR_BLOCK.get(), ResourceLocation.parse("minecraft:block/iron_block"));
        CABLES.CREATE_CENTRED(blocks, AmberCraft.Blocks.GOLD_HEAT_CONDUCTOR_BLOCK.get(), ResourceLocation.parse("minecraft:block/gold_block"));
        CABLES.CREATE_CENTRED(blocks, AmberCraft.Blocks.CREATIVE_HEAT_CONDUCTOR_BLOCK.get(), ResourceLocation.parse("ambercraft:block/creative_machine_base"));

        CABLES.CREATE_FACED(blocks, AmberCraft.Blocks.ABSTRACT_FACED_CABLE_BLOCK.get(), ResourceLocation.parse("minecraft:block/white_wool"));
    }

    private static void CREATE_HEAT_FURNACE(BlockModelGenerators g){
        var off = ModelTemplates.CUBE_ORIENTABLE.create(AmberCraft.Blocks.HEAT_FURNACE_BLOCK.get(),
                new TextureMapping()
                        .put(TextureSlot.TOP, ResourceLocation.parse(MOD_ID + ":block/brick_machine_block"))
                        .put(TextureSlot.FRONT, ResourceLocation.parse(MOD_ID + ":block/furnace_base_block"))
                        .put(TextureSlot.SIDE, ResourceLocation.parse(MOD_ID + ":block/iron_machine_base_beauty_block")),
                g.modelOutput
        );
        var on = ModelTemplates.CUBE_ORIENTABLE.create(ModelLocationUtils.getModelLocation(AmberCraft.Blocks.HEAT_FURNACE_BLOCK.get(),"_on"),
                new TextureMapping()
                        .put(TextureSlot.TOP, ResourceLocation.parse(MOD_ID + ":block/brick_machine_block"))
                        .put(TextureSlot.FRONT, ResourceLocation.parse(MOD_ID + ":block/furnace_base_on"))
                        .put(TextureSlot.SIDE, ResourceLocation.parse(MOD_ID + ":block/iron_machine_base_beauty_block")),
                g.modelOutput
        );

        g.blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(AmberCraft.Blocks.HEAT_FURNACE_BLOCK.get())
                        .with(PropertyDispatch.properties(AmberHorizontalBlock.FACING, FurnaceBlock.LIT)
                                .select(Direction.NORTH,false, Variant.variant().with(VariantProperties.MODEL, off))
                                .select(Direction.SOUTH,false, Variant.variant().with(VariantProperties.MODEL, off).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                                .select(Direction.WEST,false, Variant.variant().with(VariantProperties.MODEL, off).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                                .select(Direction.EAST,false, Variant.variant().with(VariantProperties.MODEL, off).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                                .select(Direction.NORTH,true, Variant.variant().with(VariantProperties.MODEL, on))
                                .select(Direction.SOUTH,true, Variant.variant().with(VariantProperties.MODEL, on).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                                .select(Direction.WEST,true, Variant.variant().with(VariantProperties.MODEL, on).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                                .select(Direction.EAST,true, Variant.variant().with(VariantProperties.MODEL, on).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                        )
        );
    }
}
