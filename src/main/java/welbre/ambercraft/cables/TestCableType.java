package welbre.ambercraft.cables;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import welbre.ambercraft.blockentity.FacedCableBE;
import welbre.ambercraft.module.HeatModule;
import welbre.ambercraft.module.Module;

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
    public Module[] createModules(FacedCableBE entity) {
        HeatModule module = new HeatModule();
        module.getHeatNode().setThermalConductivity(50);
        return new Module[]{module};
    }
}
