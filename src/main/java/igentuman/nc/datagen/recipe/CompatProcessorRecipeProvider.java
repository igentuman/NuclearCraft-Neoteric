package igentuman.nc.datagen.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.entries.Processors.MANUFACTORY;

public class CompatProcessorRecipeProvider implements DataProvider {

    private final PackOutput.PathProvider recipes;

    public CompatProcessorRecipeProvider(PackOutput output) {
        recipes = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> saves = new ArrayList<>();
        for (String material : List.of(
                "iron", "copper", "gold", "aluminum", "cobalt", "silver", "lead", "platinum",
                "nickel", "uranium", "osmium", "tin", "zinc", "iridium", "thorium",
                "magnesium", "lithium", "boron")) {
            saves.add(save(output, MANUFACTORY, "exdeorum/" + material + "_ore_chunk",
                    "exdeorum:" + material + "_ore_chunk", "c:dusts/" + material));
        }
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> save(CachedOutput output, String processor, String name,
                                      String inputId, String outputTag) {
        ResourceLocation.parse(inputId);
        ResourceLocation.parse(outputTag);
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", rl(processor).toString());
        recipe.addProperty("process_time", ModRecipeProvider.TIME);
        recipe.addProperty("energy_per_tick", ModRecipeProvider.ENERGY);

        JsonObject input = new JsonObject();
        input.addProperty("item", inputId);
        input.addProperty("count", 1);
        JsonArray inputs = new JsonArray();
        inputs.add(input);
        recipe.add("item_inputs", inputs);

        JsonObject result = new JsonObject();
        result.addProperty("tag", outputTag);
        result.addProperty("count", 1);
        JsonArray results = new JsonArray();
        results.add(result);
        recipe.add("item_outputs", results);

        JsonObject itemExists = new JsonObject();
        itemExists.addProperty("type", "neoforge:item_exists");
        itemExists.addProperty("item", inputId);
        JsonObject tagEmpty = new JsonObject();
        tagEmpty.addProperty("type", "neoforge:tag_empty");
        tagEmpty.addProperty("tag", outputTag);
        JsonObject tagPresent = new JsonObject();
        tagPresent.addProperty("type", "neoforge:not");
        tagPresent.add("value", tagEmpty);
        JsonArray conditions = new JsonArray();
        conditions.add(itemExists);
        conditions.add(tagPresent);
        recipe.add("neoforge:conditions", conditions);

        return DataProvider.saveStable(output, recipe, recipes.json(rl(processor + "/" + name)));
    }

    @Override
    public String getName() {
        return "NuclearCraft optional processor recipes";
    }
}
