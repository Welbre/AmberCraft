package welbre.ambercraft.cables;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import welbre.ambercraft.blockentity.FacedCableBlockEntity;
import welbre.ambercraft.module.HeatModule;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.ModulesHolder;

public class TestCableType extends CableType{
    @Override
    public Material getInsulationMaterial() {
        return new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("block/white_wool"));
    }

    @Override
    public Material getCableMaterial() {
        return new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("block/copper_block"));
    }

    @Override
    public double getWidth() {
        return 0.3;
    }

    @Override
    public double getHeight() {
        return 0.2;
    }

    @Override
    public byte getType() {
        return Types.HEAT.get();
    }


    @Override
    public Module[] createModules(FacedCableBlockEntity entity) {
        HeatModule module = new FacedHeatModule(entity);
        module.setThermalConductivity(50);
        return new Module[]{module};
    }

    public static final class FacedHeatModule extends HeatModule
    {
        public FacedHeatModule() {
        }

        BlockPos pos;
        public FacedHeatModule(BlockEntity entity) {
            super(entity);
            if (entity instanceof FacedCableBlockEntity _cable)
                this.pos = _cable.getBlockPos();
        }

        @Override
        public void transferHeatToNeighbor(Level level, BlockPos pos) {
            FacedCableBlockEntity cable = (FacedCableBlockEntity) level.getBlockEntity(pos);
            CableState state = cable.getState();

            for (Direction center : state.getCenterDirections())
            {
                for (Direction dir : CableState.GET_FACE_DIRECTIONS(center))
                {
                    //internalConnections
                    {
                        FaceBrain brain = cable.getBrain().getFaceBrain(dir);
                        if (brain != null)
                        {
                            Module[] modules = brain.getModules();
                            for (Module module : modules)
                                if (module instanceof HeatModule heat)
                                    heat.transferHeat(this, HeatModule.DEFAULT_TIME_STEP);
                            continue;
                        }
                    }

                    //externalConnections
                    {
                        if (level.getBlockEntity(pos.relative(dir)) instanceof ModulesHolder holder)
                        {
                            if (holder instanceof FacedCableBlockEntity _cable)
                            {
                                CableBrain c_brain = _cable.getBrain();
                                FaceBrain f_brain = c_brain.getFaceBrain(center);//todo problem where
                                if (f_brain != null)
                                    for (Module module : f_brain.getModules())
                                        if (module instanceof HeatModule heat)
                                            heat.transferHeat(this, DEFAULT_TIME_STEP);
                            }
                            else
                            {
                                Module[] modules = holder.getModule(dir.getOpposite());
                                for (Module module : modules)
                                {
                                    if (module instanceof HeatModule heat)
                                        this.transferHeat(heat, HeatModule.DEFAULT_TIME_STEP);
                                }
                            }
                        }
                    }

                    //diagonalConnections
                }
            }
        }

        @Override
        public void writeData(CompoundTag tag, HolderLookup.Provider registries) {
            super.writeData(tag, registries);
            tag.put("pos", BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, pos).getOrThrow());
        }

        @Override
        public void readData(CompoundTag tag, HolderLookup.Provider registries) {
            super.readData(tag, registries);
            pos = BlockPos.CODEC.parse(NbtOps.INSTANCE,tag.get("pos")).getOrThrow();
        }
    }
}
