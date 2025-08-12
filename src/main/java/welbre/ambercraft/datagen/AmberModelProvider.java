package welbre.ambercraft.datagen;

import com.google.gson.JsonObject;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.*;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.core.Direction;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blocks.heat.HeatPumpBlock;
import welbre.ambercraft.client.item.CableSpecialRender;
import welbre.ambercraft.datagen.template.AmberModelTemplate;

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

        {
            var loc = ResourceLocation.fromNamespaceAndPath(AmberCraft.MOD_ID, "block/free_z_90");
            JsonObject temp = AmberModelTemplate.FREE_BLOCK_MODEL_TEMPLATE_Z_90.createBaseTemplate(loc, new HashMap<>());
            blockModels.modelOutput.accept(loc, () -> temp);
        }
        {
            var loc = ResourceLocation.fromNamespaceAndPath(AmberCraft.MOD_ID, "block/free_z_180");
            JsonObject temp = AmberModelTemplate.FREE_BLOCK_MODEL_TEMPLATE_Z_180.createBaseTemplate(loc, new HashMap<>());
            blockModels.modelOutput.accept(loc, () -> temp);
        }
        {
            var loc = ResourceLocation.fromNamespaceAndPath(AmberCraft.MOD_ID, "block/free_z_270");
            JsonObject temp = FREE_BLOCK_MODEL_TEMPLATE_Z_270.createBaseTemplate(loc, new HashMap<>());
            blockModels.modelOutput.accept(loc, () -> temp);
        }
        {
            var loc = ResourceLocation.fromNamespaceAndPath(AmberCraft.MOD_ID, "block/free_face_xp");
            JsonObject temp = FREE_BLOCK_MODEL_TEMPLATE_FACING_XP.createBaseTemplate(loc, new HashMap<>());
            blockModels.modelOutput.accept(loc, () -> temp);
        }
        {
            var loc = ResourceLocation.fromNamespaceAndPath(AmberCraft.MOD_ID, "block/free_face_xn");
            JsonObject temp = FREE_BLOCK_MODEL_TEMPLATE_FACING_XN.createBaseTemplate(loc, new HashMap<>());
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

        blocks.blockStateOutput.accept(MultiVariantGenerator.multiVariant(AmberCraft.Blocks.HEAT_SINK_BLOCK.get(), Variant.variant().with(VariantProperties.MODEL,
                        ModelTemplates.PARTICLE_ONLY.createWithSuffix(AmberCraft.Blocks.HEAT_SINK_BLOCK.get(),"_particle", new TextureMapping().put(TextureSlot.PARTICLE, ResourceLocation.parse("ambercraft:block/copper_plate_dark")),blocks.modelOutput)
                )));
        CREATE_HEAT_PUMP(blocks);
        blocks.createTrivialCube(AmberCraft.Blocks.HEAT_SOURCE_BLOCK.get());

        CREATE_AMBER_SIDED_BLOCK(blocks, AmberCraft.Blocks.VOLTAGE_SOURCE_BLOCK.get(),
                "connection_creative_block", "connection_creative_block", "voltage_source_block");
        CREATE_AMBER_SIDED_BLOCK(blocks, AmberCraft.Blocks.RESISTOR_BLOCK.get(),
                "connection_creative_block", "connection_creative_block", "resistor_block");
        CREATE_AMBER_SIDED_BLOCK(blocks, AmberCraft.Blocks.GROUND_BLOCK.get(),
                "connection_creative_block", "ground_block","ground_block");

        CREATE_HEAT_FURNACE(blocks);
        CREATE_AMBER_FREE_BLOCK_STATE(blocks, AmberCraft.Blocks.CREATIVE_HEAT_FURNACE_BLOCK.get(), new TextureMapping()
                .put(TextureSlot.NORTH, ResourceLocation.fromNamespaceAndPath(MOD_ID, "block/creative_furnace_face_block"))
                .put(TextureSlot.SOUTH, ResourceLocation.fromNamespaceAndPath(MOD_ID, "block/connection_thermal_creative"))
                .put(TextureSlot.UP, ResourceLocation.fromNamespaceAndPath(MOD_ID, "block/connection_item_creative"))
                .put(TextureSlot.WEST, ResourceLocation.fromNamespaceAndPath(MOD_ID, "block/creative_machine_base"))
                .put(TextureSlot.EAST, ResourceLocation.fromNamespaceAndPath(MOD_ID, "block/creative_machine_base"))
                .put(TextureSlot.DOWN, ResourceLocation.fromNamespaceAndPath(MOD_ID, "block/creative_machine_base"))
                .put(TextureSlot.PARTICLE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "block/creative_machine_base")));

        CABLES.CREATE_CENTRED(blocks, AmberCraft.Blocks.COPPER_HEAT_CONDUCTOR_BLOCK.get(), ResourceLocation.parse("ambercraft:block/copper_heat_conductor"));
        CABLES.CREATE_CENTRED(blocks, AmberCraft.Blocks.IRON_HEAT_CONDUCTOR_BLOCK.get(), ResourceLocation.parse("minecraft:block/iron_block"));
        CABLES.CREATE_CENTRED(blocks, AmberCraft.Blocks.GOLD_HEAT_CONDUCTOR_BLOCK.get(), ResourceLocation.parse("minecraft:block/gold_block"));
        CABLES.CREATE_CENTRED(blocks, AmberCraft.Blocks.CREATIVE_HEAT_CONDUCTOR_BLOCK.get(), ResourceLocation.parse("ambercraft:block/creative_machine_base"));

        CABLES.CREATE_FACED(blocks, AmberCraft.Blocks.ABSTRACT_FACED_CABLE_BLOCK.get(), ResourceLocation.parse("minecraft:block/white_wool"));
    }

    private static void CREATE_HEAT_PUMP(@NotNull BlockModelGenerators blocks) {
        var model = AmberModelTemplate.CREATE_AMBER_SIDED_BLOCK_MODEL(blocks, AmberCraft.Blocks.HEAT_PUMP_BLOCK.get(), "connection_thermal_creative", "connection_thermal_creative", "heat_pump");
        blocks.blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(AmberCraft.Blocks.HEAT_PUMP_BLOCK.get())
                        .with(PropertyDispatch.property(HeatPumpBlock.FACING)
                                .select(Direction.NORTH, Variant.variant().with(VariantProperties.MODEL, model))
                                .select(Direction.SOUTH, Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                                .select(Direction.WEST, Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                                .select(Direction.EAST, Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                                .select(Direction.UP, Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
                                .select(Direction.DOWN, Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                        )
        );
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
                        .with(PropertyDispatch.properties(BlockStateProperties.HORIZONTAL_FACING, FurnaceBlock.LIT)
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
