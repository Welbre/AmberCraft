package welbre.ambercraft.subblock.layz;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.DelegateBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.subblock.TinyBlock;
import welbre.ambercraft.subblock.TinyBlockRegister;
import welbre.ambercraft.subblock.TinyBlockState;
import welbre.ambercraft.subblock.TinyItemDataComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A lazy class created to represent Simple blocks, NO VARIANTS, CUBE, with only 1 measure an integer between 1 and 16
 */
public class SimpleTinyBlock extends TinyBlock
{
    public final Block block;
    public final int size;

    public SimpleTinyBlock(ResourceLocation registerName, int size, @NotNull Block block)
    {
        super(registerName);
        if (size < 1 || size > 16) throw new IllegalArgumentException("Size must be between 1 and 16");
        if (! block.getStateDefinition().getProperties().isEmpty()) throw new IllegalArgumentException("Block must not have variants!");

        this.shape = Shapes.box(0,0,0,size / 16.0, size / 16.0, size / 16.0);
        this.block = block;
        this.size = size;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BakedModel staticModel(@Nullable TinyBlockState state)
    {
        if (state != null)
            return new SimpleTinyBlockBakedModel(Minecraft.getInstance().getBlockRenderer().getBlockModel(block.defaultBlockState()), state.getX() / 16f, state.getY() / 16f, state.getZ() / 16f, size / 16f);
        else
            return new SimpleTinyBlockBakedModel(Minecraft.getInstance().getBlockRenderer().getBlockModel(block.defaultBlockState()), 0, 0, 0, size / 16f);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void dynamicRender(TinyBlockState state)
    {

    }

    @Override
    public @NotNull Component getTinyItemName()
    {
        return Component.literal("%dx%dx%d Tiny %s".formatted(size, size, size, block.asItem().getName().getString()));
    }

    @Override
    public @Nullable ItemStack getDroppedItem(TinyBlockState state, LootParams.Builder params)
    {
        TinyBlock tinyBlock = TinyBlockRegister.FROM_STRING(registerName);
        if (tinyBlock == null) return ItemStack.EMPTY;

        return new ItemStack(
                AmberCraft.Items.TINY_ITEM, 1,
                DataComponentPatch.builder()
                        .set(AmberCraft.DataComponents.TINY_BLOCK_DATA_COMPONENT.get(), new TinyItemDataComponent(tinyBlock))
                        .build());
    }

    @Override
    public @NotNull SoundType getSoundType(TinyBlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity){
        return block.defaultBlockState().getSoundType(level, pos, entity);
    }

    @Override
    public float getDestroySpeed(TinyBlockState state, BlockGetter level, BlockPos pos)
    {
        return block.defaultBlockState().getDestroySpeed(level, pos);
    }

    @Override
    public float getPlayerDestroySpeed(Player player, TinyBlockState state, BlockGetter level, BlockPos pos) {
        return player.getDestroySpeed(block.defaultBlockState(), pos);
    }

    @Override
    public void playStepSound(@NotNull TinyBlockState tiny, @NotNull Level level, @NotNull BlockPos pos, @NotNull Entity entity)
    {
        try
        {
            var x = Entity.class.getDeclaredMethod("playStepSound", BlockPos.class, BlockState.class);
            x.setAccessible(true);
            x.invoke(entity, pos, block.defaultBlockState());
            x.setAccessible(false);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleParticles(@NotNull ClientLevel level, @NotNull BlockPos pos, @NotNull TinyBlockState state, @NotNull ParticleEngine engine, @NotNull ParticleCase particleCase, @Nullable BlockHitResult result)
    {
        switch (particleCase)
        {
            case DESTROY -> SPAWN_BREAK_PARTICLE(state, pos, engine, level);
            case HIT -> SPAWN_HIT_PARTICLE(state, pos, engine, level, result.getDirection());
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------Client Side Helpers---------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------------------------------------

    /// A modified version of {@link ParticleEngine#destroy(BlockPos, BlockState)} adapted to deal with TinyBlockState size
    @OnlyIn(Dist.CLIENT)
    private void SPAWN_BREAK_PARTICLE(TinyBlockState tiny, BlockPos pos, ParticleEngine engine, ClientLevel level)
    {
        BlockState state = this.block.defaultBlockState();
        if (!state.isAir() && !net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions.of(state).addDestroyEffects(state, level, pos, engine)) {

            getTranslatedShape(tiny).forAllBoxes(
                    (p_172273_, p_172274_, p_172275_, p_172276_, p_172277_, p_172278_) -> {
                        double d1 = Math.min(1.0, p_172276_ - p_172273_);
                        double d2 = Math.min(1.0, p_172277_ - p_172274_);
                        double d3 = Math.min(1.0, p_172278_ - p_172275_);
                        int i = Math.max(2, Mth.ceil(d1 / 0.25));
                        int j = Math.max(2, Mth.ceil(d2 / 0.25));
                        int k = Math.max(2, Mth.ceil(d3 / 0.25));
                        float volume = (float) Math.pow(d1 * d2 * d3, 1/3f);

                        for (int l = 0; l < i; l++) {
                            for (int i1 = 0; i1 < j; i1++) {
                                for (int j1 = 0; j1 < k; j1++) {
                                    double d4 = ((double)l + 0.5) / (double)i;
                                    double d5 = ((double)i1 + 0.5) / (double)j;
                                    double d6 = ((double)j1 + 0.5) / (double)k;
                                    double d7 = d4 * d1 + p_172273_;
                                    double d8 = d5 * d2 + p_172274_;
                                    double d9 = d6 * d3 + p_172275_;
                                    engine.add(
                                            new TerrainParticle(
                                                    level,
                                                    (double)pos.getX() + d7,
                                                    (double)pos.getY() + d8,
                                                    (double)pos.getZ() + d9,
                                                    d4 - 0.75,
                                                    d5 - 0.75,
                                                    d6 - 0.75,
                                                    state,
                                                    pos
                                            ).updateSprite(state, pos).scale(volume)
                                    );
                                }
                            }
                        }
                    }
            );
        }
    }

    /// A modified version of {@link ParticleEngine#crack(BlockPos, Direction)} adapted to deal with TinyBlockState size
    @OnlyIn(Dist.CLIENT)
    private void SPAWN_HIT_PARTICLE(TinyBlockState state, BlockPos pos, ParticleEngine engine, ClientLevel level, Direction side)
    {
        final RandomSource random = RandomSource.create();
        BlockState blockstate = this.block.defaultBlockState();
        if (blockstate.getRenderShape() != RenderShape.INVISIBLE && blockstate.shouldSpawnTerrainParticles()) {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            float f = 0.1F;
            AABB aabb = state.getTranslatedShape().bounds();
            final float volume = (float) Math.pow(Math.abs(aabb.maxX - aabb.minX) * Math.abs(aabb.maxY - aabb.minY) * Math.abs(aabb.maxZ - aabb.minZ) , 1/3.0);

            double d0 = (double)i + random.nextDouble() * (aabb.maxX - aabb.minX - 0.2F) + 0.1F + aabb.minX;
            double d1 = (double)j + random.nextDouble() * (aabb.maxY - aabb.minY - 0.2F) + 0.1F + aabb.minY;
            double d2 = (double)k + random.nextDouble() * (aabb.maxZ - aabb.minZ - 0.2F) + 0.1F + aabb.minZ;
            if (side == Direction.DOWN) {
                d1 = (double)j + aabb.minY - 0.1F;
            }

            if (side == Direction.UP) {
                d1 = (double)j + aabb.maxY + 0.1F;
            }

            if (side == Direction.NORTH) {
                d2 = (double)k + aabb.minZ - 0.1F;
            }

            if (side == Direction.SOUTH) {
                d2 = (double)k + aabb.maxZ + 0.1F;
            }

            if (side == Direction.WEST) {
                d0 = (double)i + aabb.minX - 0.1F;
            }

            if (side == Direction.EAST) {
                d0 = (double)i + aabb.maxX + 0.1F;
            }

            engine.add((new TerrainParticle(level, d0, d1, d2, 0.0D, 0.0D, 0.0D, blockstate, pos).updateSprite(blockstate, pos)).setPower(0.2F).scale(0.6F * volume));
        }
    }

    /**
     * Used to create the model of tiny blocks.
     */
    @OnlyIn(Dist.CLIENT)
    public static final class SimpleTinyBlockBakedModel extends DelegateBakedModel
    {
        private final float x,y,z, scale;

        public SimpleTinyBlockBakedModel(BakedModel parent, float x, float y, float z, float scale)
        {
            super(parent);
            this.x = x;
            this.y = y;
            this.z = z;
            this.scale = scale;
        }

        @Override
        public @NotNull List<BakedQuad> getQuads(@Nullable BlockState p_371320_, @Nullable Direction p_371369_, @NotNull RandomSource p_371947_) {
            //called by the item render
            List<BakedQuad> quads = super.getQuads(p_371320_, p_371369_, p_371947_);
            return SCALE_AND_MOVE(quads, x, y, z, scale);
        }

        @Override
        public @NotNull List<BakedQuad> getQuads(@Nullable BlockState p_371320_, @Nullable Direction p_371369_, @NotNull RandomSource p_371947_, @NotNull ModelData modelData, @Nullable RenderType renderType)
        {
            //called by block render
            List<BakedQuad> quads = super.getQuads(p_371320_, p_371369_, p_371947_, modelData, renderType);
            return SCALE_AND_MOVE(quads, x, y, z, scale);
        }

        @Override
        public void applyTransform(@NotNull ItemDisplayContext transformType, @NotNull PoseStack poseStack, boolean applyLeftHandTransform)
        {
            super.applyTransform(transformType, poseStack, applyLeftHandTransform);
            poseStack.translate(0.5 - scale / 2f, 0.5 - scale / 2f, 0.5 - scale / 2f);
        }

        public static List<BakedQuad> SCALE_AND_MOVE(Collection<BakedQuad> quads, float x, float y, float z, float scale)
        {
            ArrayList<BakedQuad> list = new ArrayList<>(quads.size());
            for (BakedQuad quad : quads)
            {
                int[] vertices = quad.getVertices().clone();
                for (int i = 0; i < 4; i++)
                {
                    int index = i * 8;

                    float sx = Float.intBitsToFloat(vertices[index]);
                    float sy = Float.intBitsToFloat(vertices[index + 1]);
                    float sz = Float.intBitsToFloat(vertices[index + 2]);

                    sx = sx * scale + x;
                    sy = sy * scale + y;
                    sz = sz * scale + z;

                    vertices[index] = Float.floatToRawIntBits(sx);
                    vertices[index + 1] = Float.floatToRawIntBits(sy);
                    vertices[index + 2] = Float.floatToRawIntBits(sz);
                }
                list.add(new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade(), quad.getLightEmission(), quad.hasAmbientOcclusion()));
            }

            return list;
        }
    }
}
