package welbre.ambercraft.client.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import welbre.ambercraft.Main;

public class CableModelLoader implements UnbakedModelLoader<CableModelLoader.CableModelGeometry> {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,"cable_loader");

    @Override
    public CableModelGeometry read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
        return new CableModelGeometry();
    }

    public static final class CableModelGeometry implements UnbakedModel {
        @Override
        public BakedModel bake(TextureSlots textureSlots, ModelBaker baker, ModelState modelState, boolean hasAmbientOcclusion, boolean useBlockLight, ItemTransforms transforms) {
            return new CableBakedModel(baker, transforms, textureSlots);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {

        }
    }
}
