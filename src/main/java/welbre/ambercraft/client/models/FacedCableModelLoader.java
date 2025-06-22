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
import org.jetbrains.annotations.NotNull;

public class FacedCableModelLoader implements UnbakedModelLoader<FacedCableModelLoader.Geometry> {

    @Override
    public Geometry read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
        return new Geometry(jsonObject);
    }

    public static final class Geometry implements UnbakedModel {
        final JsonObject json;

        public Geometry(JsonObject json) {
            this.json = json;
        }

        @Override
        public BakedModel bake(TextureSlots textureSlots, ModelBaker baker, ModelState modelState, boolean hasAmbientOcclusion, boolean useBlockLight, ItemTransforms transforms) {
            return new FacedCableBakedModel(textureSlots, baker, modelState, transforms);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {

        }

        @Override
        public TextureSlots.@NotNull Data getTextureSlots() {
            return TextureSlots.parseTextureMap(json.getAsJsonObject("textures"), TextureAtlas.LOCATION_BLOCKS);
        }
    }
}
