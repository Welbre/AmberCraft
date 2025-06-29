package welbre.ambercraft.cables;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;

public class TestCableType extends CableType{
    @Override
    public Material getInsulationMaterial() {
        return new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("block/white_wool"));
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
}
