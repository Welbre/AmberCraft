package welbre.ambercraft.client.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

public class CentredCableModelLoader implements UnbakedModelLoader<CentredCableModelLoader.CableModelGeometry> {
    @Override
    public CableModelGeometry read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
        return new CableModelGeometry(jsonObject);
    }

    public static final class CableModelGeometry implements UnbakedModel {
        final JsonObject json;

        public CableModelGeometry(JsonObject json) {
            this.json = json;
        }

        @Override
        public BakedModel bake(TextureSlots textureSlots, ModelBaker baker, ModelState modelState, boolean hasAmbientOcclusion, boolean useBlockLight, ItemTransforms transforms) {
            return new CentredCableBakedModel(baker, transforms, textureSlots);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {

        }

        @Override
        public TextureSlots.Data getTextureSlots() {
            return TextureSlots.parseTextureMap(json.getAsJsonObject("textures"), TextureAtlas.LOCATION_BLOCKS);
        }
    }
}
