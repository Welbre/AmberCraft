package welbre.ambercraft.datagen;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.blockstates.Variant;
import net.minecraft.client.data.models.blockstates.VariantProperties;
import net.minecraft.client.data.models.model.*;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import net.neoforged.neoforge.client.model.generators.template.FaceRotation;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.Main;
import welbre.ambercraft.blocks.VoltageSourceBlock;

public class AmberModelProvider extends ModelProvider {
    public static final TextureSlot connection0 = TextureSlot.create("c0");
    public static final TextureSlot connection1 = TextureSlot.create("c1");
    public static final TextureSlot base = TextureSlot.create("base");

    public static final ModelTemplate COMPONENT_TEMPLATE = ExtendedModelTemplateBuilder.builder()
            .parent(ResourceLocation.parse("minecraft:block/block"))
            .requiredTextureSlot(connection0)
            .requiredTextureSlot(connection1)
            .requiredTextureSlot(base)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .element(cons -> {
                        cons.face(Direction.DOWN, face -> face.uvs(0, 0, 16, 16).texture(base).rotation(FaceRotation.UPSIDE_DOWN).cullface(Direction.DOWN));
                        cons.face(Direction.UP, face -> face.uvs(0, 0, 16, 16).texture(base).cullface(Direction.UP));
                        cons.face(Direction.NORTH, face -> face.uvs(0, 0, 16, 16).texture(connection0).cullface(Direction.NORTH));
                        cons.face(Direction.SOUTH, face -> face.uvs(0, 0, 16, 16).texture(connection1).cullface(Direction.SOUTH));
                        cons.face(Direction.WEST, face -> face.uvs(0, 0, 16, 16).texture(base).rotation(FaceRotation.COUNTERCLOCKWISE_90).cullface(Direction.WEST));
                        cons.face(Direction.EAST, face -> face.uvs(0, 0, 16, 16).texture(base).rotation(FaceRotation.CLOCKWISE_90).cullface(Direction.EAST));
                    }
            ).build();

    public AmberModelProvider(PackOutput output) {
        super(output, Main.MOD_ID);
    }

    @Override
    protected void registerModels(@NotNull BlockModelGenerators blockModels, @NotNull ItemModelGenerators itemModels) {
        blockModels.modelOutput.accept(null, new );
        itemModels.generateFlatItem(Main.Items.MULTIMETER.get(), ModelTemplates.FLAT_ITEM);

        blockModels.createTrivialCube(Main.Blocks.IRON_MACHINE_CASE_BLOCK.get());

        CREATE_AMBER_2_AXES(blockModels, Main.Blocks.VOLTAGE_SOURCE_BLOCK.get(),
                "connection_creative_block", "connection_creative_block", "voltage_source_block");
        CREATE_AMBER_2_AXES(blockModels, Main.Blocks.RESISTOR_BLOCK.get(),
                "connection_creative_block", "connection_creative_block", "resistor_block");
    }

    private static void CREATE_AMBER_2_AXES_BLOCK_STATE(BlockModelGenerators g, Block block, ResourceLocation model){
        g.blockStateOutput.accept(
            MultiVariantGenerator.multiVariant(block)
                    .with(PropertyDispatch.property(VoltageSourceBlock.FACING)
                            .select(Direction.DOWN,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                            .select(Direction.UP,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
                            .select(Direction.NORTH,Variant.variant().with(VariantProperties.MODEL, model))
                            .select(Direction.SOUTH,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                            .select(Direction.WEST,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                            .select(Direction.EAST,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                    )
        );
    }

    private static ResourceLocation CREATE_AMBER_2_AXES_MODEL(BlockModelGenerators g, Block block, String c0, String c1, String sides){
        return COMPONENT_TEMPLATE.create(block,
                new TextureMapping()
                        .put(connection0, ResourceLocation.parse(Main.MOD_ID + ":block/"+c0))
                        .put(connection1, ResourceLocation.parse(Main.MOD_ID + ":block/"+c1))
                        .put(base, ResourceLocation.parse(Main.MOD_ID + ":block/"+sides))
                        .put(TextureSlot.PARTICLE, ResourceLocation.parse(Main.MOD_ID + ":block/voltage_source_block")),
                g.modelOutput
        );
    }

    private static void CREATE_AMBER_2_AXES(BlockModelGenerators g, Block b, String c0, String c1, String sides){
        CREATE_AMBER_2_AXES_BLOCK_STATE(g, b, CREATE_AMBER_2_AXES_MODEL(g,b,c0,c1,sides));
    }
}
