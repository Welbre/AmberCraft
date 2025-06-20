package welbre.ambercraft.datagen.template;

import com.google.gson.JsonObject;
import net.neoforged.neoforge.client.model.generators.template.CustomLoaderBuilder;
import welbre.ambercraft.client.models.CableModelLoader;

import java.util.function.Supplier;

public class MyLoaderBuilder extends CustomLoaderBuilder {
    public MyLoaderBuilder() {
        super(
                // Your model loader's id.
                CableModelLoader.ID,
                // Whether the loader allows inline vanilla elements as a fallback if the loader is absent.
                false
        );
    }

    // Add fields and setters for the fields here. The fields can then be used below.

    @Override
    protected CustomLoaderBuilder copyInternal() {
        // Create a new instance of your loader builder and copy the properties from this builder
        // to the new instance.
        MyLoaderBuilder builder = new MyLoaderBuilder();
        // builder.<field> = this.<field>;
        return builder;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        return super.toJson(json);
    }
}
