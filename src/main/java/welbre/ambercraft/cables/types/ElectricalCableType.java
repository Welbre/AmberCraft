package welbre.ambercraft.cables.types;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import welbre.ambercraft.AmberCraft;
import welbre.ambercraft.blockentity.FacedCableBE;
import welbre.ambercraft.cables.CableType;
import welbre.ambercraft.module.Module;
import welbre.ambercraft.module.electrical.ElectricalCableModule;

public class ElectricalCableType extends CableType {
    @Override
    public double getWidth() {
        return 0.2;
    }

    @Override
    public double getHeight() {
        return 0.1;
    }

    @Override
    public Material getInsulationMaterial() {
        return new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.fromNamespaceAndPath(AmberCraft.MOD_ID,"block/cable/rubber"));
    }

    @Override
    public Material getCableMaterial() {
        return new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("block/copper_block"));
    }

    @Override
    public byte getType() {
        return Types.ELECTRIC.get();
    }

    @Override
    public Module[] createModules(FacedCableBE entity) {
        return new Module[]{new ElectricalCableModule(0.1)};
    }
}
