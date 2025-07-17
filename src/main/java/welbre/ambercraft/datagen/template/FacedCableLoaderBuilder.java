package welbre.ambercraft.datagen.template;

import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.template.CustomLoaderBuilder;
import welbre.ambercraft.AmberCraft;

public class FacedCableLoaderBuilder extends CustomLoaderBuilder {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(AmberCraft.MOD_ID,"faced_cable_loader");
    public static final TextureSlot CABLE = TextureSlot.create("cable");

    @Override
    protected CustomLoaderBuilder copyInternal() {
        return new CentredCableLoaderBuilder();
    }

    public FacedCableLoaderBuilder() {
        super(ID, false);
    }
}
