package welbre.ambercraft.datagen.template;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.blockstates.Variant;
import net.minecraft.client.data.models.blockstates.VariantProperties;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import net.neoforged.neoforge.client.model.generators.template.FaceRotation;
import welbre.ambercraft.Main;
import welbre.ambercraft.blocks.parent.AmberFreeBlock;
import welbre.ambercraft.blocks.parent.AmberHorizontalBlock;
import welbre.ambercraft.blocks.parent.AmberSidedBasicBlock;
import welbre.ambercraft.blocks.VoltageSourceBlockAmberBasic;

import static welbre.ambercraft.Main.MOD_ID;

public class AmberModelTemplate {
    public static final TextureSlot connection0 = TextureSlot.create("c0");
    public static final TextureSlot connection1 = TextureSlot.create("c1");
    public static final TextureSlot base = TextureSlot.create("base");

    //can face to all 6 directions, up, down, east, west, north, south.
    public static final ModelTemplate SIDED_BLOCK_TEMPLATE = ExtendedModelTemplateBuilder.builder() // locate at asserts\ambercraft\models\blocks\sided_block
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
    public static final ModelTemplate SIDED_BLOCK = ExtendedModelTemplateBuilder.builder()
            .parent(ResourceLocation.fromNamespaceAndPath(MOD_ID,"block/sided_block"))
            .requiredTextureSlot(connection0)
            .requiredTextureSlot(connection1)
            .requiredTextureSlot(base)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .build();

    public static void CREATE_AMBER_FREE_BLOCK_STATE(BlockModelGenerators g, AmberFreeBlock block, ResourceLocation model){
        g.blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(block)//todo finish this
                        .with(PropertyDispatch.properties(AmberFreeBlock.FACING, AmberFreeBlock.ROTATION)
                                .select(Direction.DOWN, AmberFreeBlock.FaceRotation.UP,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                                .select(Direction.UP, AmberFreeBlock.FaceRotation.UP,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
                                .select(Direction.NORTH, AmberFreeBlock.FaceRotation.UP,Variant.variant().with(VariantProperties.MODEL, model))
                                .select(Direction.SOUTH, AmberFreeBlock.FaceRotation.UP,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                                .select(Direction.WEST, AmberFreeBlock.FaceRotation.UP,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                                .select(Direction.EAST, AmberFreeBlock.FaceRotation.UP,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                        )
        );
    }

    public static void CREATE_AMBER_SIDED_BLOCK_STATE(BlockModelGenerators g, AmberSidedBasicBlock block, ResourceLocation model){
        g.blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(block)
                        .with(PropertyDispatch.property(VoltageSourceBlockAmberBasic.FACING)
                                .select(Direction.DOWN, Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                                .select(Direction.UP,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
                                .select(Direction.NORTH,Variant.variant().with(VariantProperties.MODEL, model))
                                .select(Direction.SOUTH,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                                .select(Direction.WEST,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                                .select(Direction.EAST,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                        )
        );
    }

    public static void CREATE_AMBER_HORIZONTAL_BLOCK(BlockModelGenerators g, AmberHorizontalBlock block, ResourceLocation model){
        g.blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(block)
                        .with(PropertyDispatch.property(AmberHorizontalBlock.FACING)
                                .select(Direction.NORTH, Variant.variant().with(VariantProperties.MODEL, model))
                                .select(Direction.SOUTH, Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                                .select(Direction.WEST,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                                .select(Direction.EAST,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                        )
        );
    }

    public static ResourceLocation CREATE_AMBER_SIDED_BLOCK_MODEL(BlockModelGenerators g, Block block, String c0, String c1, String sides){
        return SIDED_BLOCK.create(block,
                new TextureMapping()
                        .put(AmberModelTemplate.connection0, ResourceLocation.parse(MOD_ID + ":block/"+c0))
                        .put(AmberModelTemplate.connection1, ResourceLocation.parse(MOD_ID + ":block/"+c1))
                        .put(AmberModelTemplate.base, ResourceLocation.parse(MOD_ID + ":block/"+sides))
                        .put(TextureSlot.PARTICLE, ResourceLocation.parse(MOD_ID + ":block/"+sides)),
                g.modelOutput
        );
    }

    public static void CREATE_AMBER_SIDED_BLOCK(BlockModelGenerators g, AmberSidedBasicBlock block, String connectionTexture0, String connectionTexture1, String sides){
        CREATE_AMBER_SIDED_BLOCK_STATE(g, block, CREATE_AMBER_SIDED_BLOCK_MODEL(g,block,connectionTexture0,connectionTexture1,sides));
    }

    public static void CREATE_AMBER_FREE_BLOCK(BlockModelGenerators g, AmberFreeBlock block, String connectionTexture0, String connectionTexture1, String sides){
        CREATE_AMBER_FREE_BLOCK_STATE(g, block, CREATE_AMBER_SIDED_BLOCK_MODEL(g, block, connectionTexture0, connectionTexture1, sides));
    }

    public static void CREATE_AMBER_HORIZONTAL_BLOCK(BlockModelGenerators g, AmberHorizontalBlock block, String top, String front, String sides){
        CREATE_AMBER_HORIZONTAL_BLOCK(g, block, ModelTemplates.CUBE_ORIENTABLE.create(block,
                new TextureMapping()
                        .put(TextureSlot.TOP, ResourceLocation.parse(MOD_ID + ":block/"+top))
                        .put(TextureSlot.FRONT, ResourceLocation.parse(MOD_ID + ":block/"+front))
                        .put(TextureSlot.SIDE, ResourceLocation.parse(MOD_ID + ":block/"+sides)),
                g.modelOutput));
    }
}
