package welbre.ambercraft.datagen.template;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.blockstates.Variant;
import net.minecraft.client.data.models.blockstates.VariantProperties;
import net.minecraft.client.data.models.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import net.neoforged.neoforge.client.model.generators.template.FaceRotation;
import org.jetbrains.annotations.NotNull;

import static welbre.ambercraft.AmberCraft.MOD_ID;

public class AmberModelTemplate {
    public static final EnumProperty<Rotation> ROTATION = EnumProperty.create("rotation", Rotation.class);
    public static final TextureSlot connection0 = TextureSlot.create("c0");
    public static final TextureSlot connection1 = TextureSlot.create("c1");
    public static final TextureSlot base = TextureSlot.create("base");

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------Templates-----------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    ///can face to all 6 directions, up, down, east, west, north, south.
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


    public static final ModelTemplate FREE_BLOCK_MODEL_TEMPLATE_Z_270 = ExtendedModelTemplateBuilder.builder()
            .parent(ResourceLocation.parse("ambercraft:block/block"))
            .requiredTextureSlot(TextureSlot.NORTH)
            .requiredTextureSlot(TextureSlot.SOUTH)
            .requiredTextureSlot(TextureSlot.WEST)
            .requiredTextureSlot(TextureSlot.EAST)
            .requiredTextureSlot(TextureSlot.UP)
            .requiredTextureSlot(TextureSlot.DOWN)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .element( cons -> {
                cons.from(0, 0, 0).to(16, 16, 16);
                cons.face(Direction.NORTH, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.NORTH).rotation(FaceRotation.CLOCKWISE_90).cullface(Direction.NORTH));
                cons.face(Direction.SOUTH, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.SOUTH).rotation(FaceRotation.COUNTERCLOCKWISE_90).cullface(Direction.SOUTH));
                cons.face(Direction.WEST, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.UP).rotation(FaceRotation.COUNTERCLOCKWISE_90).cullface(Direction.WEST));
                cons.face(Direction.EAST, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.DOWN).rotation(FaceRotation.COUNTERCLOCKWISE_90).cullface(Direction.EAST));
                cons.face(Direction.UP, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.EAST).rotation(FaceRotation.COUNTERCLOCKWISE_90).cullface(Direction.UP));
                cons.face(Direction.DOWN, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.WEST).rotation(FaceRotation.COUNTERCLOCKWISE_90).cullface(Direction.DOWN));
            })
            .suffix("_z_270")
            .build();

    public static final ModelTemplate FREE_BLOCK_MODEL_TEMPLATE_Z_180 = ExtendedModelTemplateBuilder.builder()
            .parent(ResourceLocation.parse("ambercraft:block/block"))
            .requiredTextureSlot(TextureSlot.NORTH)
            .requiredTextureSlot(TextureSlot.SOUTH)
            .requiredTextureSlot(TextureSlot.WEST)
            .requiredTextureSlot(TextureSlot.EAST)
            .requiredTextureSlot(TextureSlot.UP)
            .requiredTextureSlot(TextureSlot.DOWN)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .element( cons -> {
                cons.from(0, 0, 0).to(16, 16, 16);
                cons.face(Direction.NORTH, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.NORTH).rotation(FaceRotation.UPSIDE_DOWN).cullface(Direction.NORTH));
                cons.face(Direction.SOUTH, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.SOUTH).rotation(FaceRotation.UPSIDE_DOWN).cullface(Direction.SOUTH));
                cons.face(Direction.WEST, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.EAST).rotation(FaceRotation.UPSIDE_DOWN).cullface(Direction.WEST));
                cons.face(Direction.EAST, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.WEST).rotation(FaceRotation.UPSIDE_DOWN).cullface(Direction.EAST));
                cons.face(Direction.UP, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.DOWN).rotation(FaceRotation.UPSIDE_DOWN).cullface(Direction.UP));
                cons.face(Direction.DOWN, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.UP).rotation(FaceRotation.UPSIDE_DOWN).cullface(Direction.DOWN));
            })
            .suffix("_z_180")
            .build();

    public static final ModelTemplate FREE_BLOCK_MODEL_TEMPLATE_Z_90 = ExtendedModelTemplateBuilder.builder()
            .parent(ResourceLocation.parse("ambercraft:block/block"))
            .requiredTextureSlot(TextureSlot.NORTH)
            .requiredTextureSlot(TextureSlot.SOUTH)
            .requiredTextureSlot(TextureSlot.WEST)
            .requiredTextureSlot(TextureSlot.EAST)
            .requiredTextureSlot(TextureSlot.UP)
            .requiredTextureSlot(TextureSlot.DOWN)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .element( cons -> {
                cons.from(0, 0, 0).to(16, 16, 16);
                cons.face(Direction.NORTH, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.NORTH).rotation(FaceRotation.COUNTERCLOCKWISE_90).cullface(Direction.NORTH));
                cons.face(Direction.SOUTH, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.SOUTH).rotation(FaceRotation.CLOCKWISE_90).cullface(Direction.SOUTH));
                cons.face(Direction.WEST, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.DOWN).rotation(FaceRotation.CLOCKWISE_90).cullface(Direction.WEST));
                cons.face(Direction.EAST, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.UP).rotation(FaceRotation.CLOCKWISE_90).cullface(Direction.EAST));
                cons.face(Direction.UP, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.WEST).rotation(FaceRotation.CLOCKWISE_90).cullface(Direction.UP));
                cons.face(Direction.DOWN, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.EAST).rotation(FaceRotation.CLOCKWISE_90).cullface(Direction.DOWN));
            })
            .suffix("_z_90")
            .build();

    public static final ModelTemplate FREE_BLOCK_MODEL_TEMPLATE_FACING_XN = ExtendedModelTemplateBuilder.builder()
            .parent(ResourceLocation.parse("ambercraft:block/block"))
            .requiredTextureSlot(TextureSlot.NORTH)
            .requiredTextureSlot(TextureSlot.SOUTH)
            .requiredTextureSlot(TextureSlot.WEST)
            .requiredTextureSlot(TextureSlot.EAST)
            .requiredTextureSlot(TextureSlot.UP)
            .requiredTextureSlot(TextureSlot.DOWN)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .element( cons -> {
                cons.from(0, 0, 0).to(16, 16, 16);
                cons.face(Direction.NORTH, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.EAST).cullface(Direction.NORTH));
                cons.face(Direction.SOUTH, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.WEST).cullface(Direction.SOUTH));
                cons.face(Direction.WEST, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.NORTH).cullface(Direction.WEST));
                cons.face(Direction.EAST, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.SOUTH).cullface(Direction.EAST));
                cons.face(Direction.UP, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.UP).rotation(FaceRotation.COUNTERCLOCKWISE_90).cullface(Direction.UP));
                cons.face(Direction.DOWN, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.DOWN).rotation(FaceRotation.CLOCKWISE_90).cullface(Direction.DOWN));
            })
            .suffix("_xn")
            .build();

    public static final ModelTemplate FREE_BLOCK_MODEL_TEMPLATE_FACING_XP = ExtendedModelTemplateBuilder.builder()
            .parent(ResourceLocation.parse("ambercraft:block/block"))
            .requiredTextureSlot(TextureSlot.NORTH)
            .requiredTextureSlot(TextureSlot.SOUTH)
            .requiredTextureSlot(TextureSlot.WEST)
            .requiredTextureSlot(TextureSlot.EAST)
            .requiredTextureSlot(TextureSlot.UP)
            .requiredTextureSlot(TextureSlot.DOWN)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .element( cons -> {
                cons.from(0, 0, 0).to(16, 16, 16);
                cons.face(Direction.NORTH, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.WEST).cullface(Direction.NORTH));
                cons.face(Direction.SOUTH, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.EAST).cullface(Direction.SOUTH));
                cons.face(Direction.WEST, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.SOUTH).cullface(Direction.WEST));
                cons.face(Direction.EAST, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.NORTH).cullface(Direction.EAST));
                cons.face(Direction.UP, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.UP).rotation(FaceRotation.CLOCKWISE_90).cullface(Direction.UP));
                cons.face(Direction.DOWN, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.DOWN).rotation(FaceRotation.COUNTERCLOCKWISE_90).cullface(Direction.DOWN));
            })
            .suffix("_xp")
            .build();

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------Templates users-----------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public static final ModelTemplate SIDED_BLOCK_USER = ExtendedModelTemplateBuilder.builder()
            .parent(ResourceLocation.fromNamespaceAndPath(MOD_ID,"block/sided_block"))
            .requiredTextureSlot(connection0)
            .requiredTextureSlot(connection1)
            .requiredTextureSlot(base)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .build();

    public static void CREATE_AMBER_FREE_BLOCK_STATE(BlockModelGenerators g, Block block, TextureMapping mapping){
        var model = ModelTemplates.CUBE.create(block, mapping, g.modelOutput);
        var z90 = FREE_BLOCK_MODEL_TEMPLATE_Z_90.create(block, mapping, g.modelOutput);
        var z180 = FREE_BLOCK_MODEL_TEMPLATE_Z_180.create(block, mapping, g.modelOutput);
        var z270 = FREE_BLOCK_MODEL_TEMPLATE_Z_270.create(block, mapping, g.modelOutput);

        var xp = FREE_BLOCK_MODEL_TEMPLATE_FACING_XP.create(block, mapping, g.modelOutput);
        var xn = FREE_BLOCK_MODEL_TEMPLATE_FACING_XN.create(block, mapping, g.modelOutput);

        g.blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(block)
                        .with(PropertyDispatch.properties(BlockStateProperties.FACING, ROTATION)
                                .select(Direction.NORTH, Rotation.NONE,Variant.variant().with(VariantProperties.MODEL, model))
                                .select(Direction.NORTH, Rotation.COUNTERCLOCKWISE_90,Variant.variant().with(VariantProperties.MODEL, z90))
                                .select(Direction.NORTH, Rotation.CLOCKWISE_180,Variant.variant().with(VariantProperties.MODEL, z180))
                                .select(Direction.NORTH, Rotation.CLOCKWISE_90,Variant.variant().with(VariantProperties.MODEL, z270))

                                .select(Direction.SOUTH, Rotation.NONE,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                                .select(Direction.SOUTH, Rotation.COUNTERCLOCKWISE_90,Variant.variant().with(VariantProperties.MODEL, z90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                                .select(Direction.SOUTH, Rotation.CLOCKWISE_180,Variant.variant().with(VariantProperties.MODEL, z180).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                                .select(Direction.SOUTH, Rotation.CLOCKWISE_90,Variant.variant().with(VariantProperties.MODEL, z270).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))

                                .select(Direction.WEST, Rotation.NONE,Variant.variant().with(VariantProperties.MODEL, xn))
                                .select(Direction.WEST, Rotation.COUNTERCLOCKWISE_90,Variant.variant().with(VariantProperties.MODEL, xn).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                                .select(Direction.WEST, Rotation.CLOCKWISE_180,Variant.variant().with(VariantProperties.MODEL, xn).with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
                                .select(Direction.WEST, Rotation.CLOCKWISE_90,Variant.variant().with(VariantProperties.MODEL, xn).with(VariantProperties.X_ROT, VariantProperties.Rotation.R270))

                                .select(Direction.EAST, Rotation.NONE,Variant.variant().with(VariantProperties.MODEL, xp))
                                .select(Direction.EAST, Rotation.COUNTERCLOCKWISE_90,Variant.variant().with(VariantProperties.MODEL, xp).with(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
                                .select(Direction.EAST, Rotation.CLOCKWISE_180,Variant.variant().with(VariantProperties.MODEL, xp).with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
                                .select(Direction.EAST, Rotation.CLOCKWISE_90,Variant.variant().with(VariantProperties.MODEL, xp).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))

                                .select(Direction.UP, Rotation.NONE,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
                                .select(Direction.UP, Rotation.COUNTERCLOCKWISE_90,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.X_ROT, VariantProperties.Rotation.R270).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                                .select(Direction.UP, Rotation.CLOCKWISE_180,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.X_ROT, VariantProperties.Rotation.R270).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                                .select(Direction.UP, Rotation.CLOCKWISE_90,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.X_ROT, VariantProperties.Rotation.R270).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))

                                .select(Direction.DOWN, Rotation.NONE, Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                                .select(Direction.DOWN, Rotation.COUNTERCLOCKWISE_90,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                                .select(Direction.DOWN, Rotation.CLOCKWISE_180,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                                .select(Direction.DOWN, Rotation.CLOCKWISE_90,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                        )
        );
    }

    public static void CREATE_AMBER_SIDED_BLOCK_STATE(BlockModelGenerators g, Block block, ResourceLocation model){
        g.blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(block)
                        .with(PropertyDispatch.property(BlockStateProperties.FACING)
                                .select(Direction.DOWN, Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                                .select(Direction.UP,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
                                .select(Direction.NORTH,Variant.variant().with(VariantProperties.MODEL, model))
                                .select(Direction.SOUTH,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                                .select(Direction.WEST,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                                .select(Direction.EAST,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                        )
        );
    }

    public static void CREATE_AMBER_HORIZONTAL_BLOCK(BlockModelGenerators g, Block block, ResourceLocation model){
        g.blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(block)
                        .with(PropertyDispatch.property(BlockStateProperties.FACING)
                                .select(Direction.NORTH, Variant.variant().with(VariantProperties.MODEL, model))
                                .select(Direction.SOUTH, Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                                .select(Direction.WEST,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                                .select(Direction.EAST,Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                        )
        );
    }

    public static ResourceLocation CREATE_AMBER_SIDED_BLOCK_MODEL(BlockModelGenerators g, Block block, String c0, String c1, String sides){
        return SIDED_BLOCK_USER.create(block,
                new TextureMapping()
                        .put(AmberModelTemplate.connection0, ResourceLocation.parse(MOD_ID + ":block/"+c0))
                        .put(AmberModelTemplate.connection1, ResourceLocation.parse(MOD_ID + ":block/"+c1))
                        .put(AmberModelTemplate.base, ResourceLocation.parse(MOD_ID + ":block/"+sides))
                        .put(TextureSlot.PARTICLE, ResourceLocation.parse(MOD_ID + ":block/"+sides)),
                g.modelOutput
        );
    }

    public static ResourceLocation CREATE_MINECRAFT_CUBE_BLOCK_MODEL(BlockModelGenerators g, Block block, String north, String south, String east, String west, String up, String down, String particle) {
        return ModelTemplates.CUBE.create(block,
                new TextureMapping()
                        .put(TextureSlot.NORTH, ResourceLocation.parse(MOD_ID + ":block/"+north))
                        .put(TextureSlot.SOUTH, ResourceLocation.parse(MOD_ID + ":block/"+south))
                        .put(TextureSlot.EAST, ResourceLocation.parse(MOD_ID + ":block/"+east))
                        .put(TextureSlot.WEST, ResourceLocation.parse(MOD_ID + ":block/"+west))
                        .put(TextureSlot.UP, ResourceLocation.parse(MOD_ID + ":block/"+up))
                        .put(TextureSlot.DOWN, ResourceLocation.parse(MOD_ID + ":block/"+down))
                        .put(TextureSlot.PARTICLE, ResourceLocation.parse(MOD_ID + ":block/"+particle)),
                g.modelOutput
        );
    }

    public static void CREATE_MINECRAFT_BLOCK(BlockModelGenerators g, Block block, String north, String south, String east, String west, String up, String down, String particle){
        g.blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(
                        block, Variant.variant().with(VariantProperties.MODEL,
                                CREATE_MINECRAFT_CUBE_BLOCK_MODEL(g,block,north,south,east,west,up,down, particle))));
    }

    public static void CREATE_AMBER_SIDED_BLOCK(BlockModelGenerators g, Block block, String connectionTexture0, String connectionTexture1, String sides){
        CREATE_AMBER_SIDED_BLOCK_STATE(g, block, CREATE_AMBER_SIDED_BLOCK_MODEL(g,block,connectionTexture0,connectionTexture1,sides));
    }

    public static void CREATE_AMBER_HORIZONTAL_BLOCK(BlockModelGenerators g, Block block, String top, String front, String sides){
        CREATE_AMBER_HORIZONTAL_BLOCK(g, block, ModelTemplates.CUBE_ORIENTABLE.create(block,
                new TextureMapping()
                        .put(TextureSlot.TOP, ResourceLocation.parse(MOD_ID + ":block/"+top))
                        .put(TextureSlot.FRONT, ResourceLocation.parse(MOD_ID + ":block/"+front))
                        .put(TextureSlot.SIDE, ResourceLocation.parse(MOD_ID + ":block/"+sides)),
                g.modelOutput));
    }

    public static final class CABLES {
        public static void CREATE_CENTRED(@NotNull BlockModelGenerators g, Block block, ResourceLocation texture){
            g.createTrivialBlock(block, TexturedModel.createDefault(
                    block1 -> new TextureMapping()
                            .put(CentredCableLoaderBuilder.CABLE, texture),
                    ExtendedModelTemplateBuilder.builder()
                            .customLoader(CentredCableLoaderBuilder::new, loader -> {})
                            .requiredTextureSlot(CentredCableLoaderBuilder.CABLE)
                            .build()
            ));
        }

        public static void CREATE_FACED(@NotNull BlockModelGenerators g, Block block, ResourceLocation texture){
            g.createTrivialBlock(block, TexturedModel.createDefault(
                    block1 -> new TextureMapping()
                            .put(FacedCableLoaderBuilder.CABLE, texture),
                    ExtendedModelTemplateBuilder.builder()
                            .customLoader(FacedCableLoaderBuilder::new, loader -> {})
                            .requiredTextureSlot(FacedCableLoaderBuilder.CABLE)
                            .build()
            ));
        }
    }
}
