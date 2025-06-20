package welbre.ambercraft.datagen.template;

import com.google.gson.JsonObject;
import net.minecraft.client.data.models.model.TextureSlot;
import net.neoforged.neoforge.client.model.generators.template.CustomLoaderBuilder;
import org.jetbrains.annotations.NotNull;
import welbre.ambercraft.client.models.CableModelLoader;

public class CentredCableLoaderBuilder extends CustomLoaderBuilder {
    public static final TextureSlot CABLE = TextureSlot.create("cable");
    public static final TextureSlot INSULATION = TextureSlot.create("insulation");
    public float radius = 0;

    public CentredCableLoaderBuilder() {
        super(
                // Your model loader's id.
                CableModelLoader.ID,
                // Whether the loader allows inline vanilla elements as a fallback if the loader is absent.
                false
        );
    }

    // Add fields and setters for the fields here. The fields can then be used below.

    @Override
    protected @NotNull CustomLoaderBuilder copyInternal() {
        // Create a new instance of your loader builder and copy the properties from this builder
        // to the new instance.
        CentredCableLoaderBuilder builder = new CentredCableLoaderBuilder();
        builder.radius = this.radius;
        // builder.<field> = this.<field>;
        return builder;
    }

    @Override
    public @NotNull JsonObject toJson(JsonObject json) {
        json.addProperty("radius", radius);
        return super.toJson(json);
    }
}
