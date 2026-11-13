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
import static igentuman.nc.setup.entries.Processors.ALLOY_SMELTER;
import static igentuman.nc.setup.entries.Processors.ASSEMBLER;
import static igentuman.nc.setup.entries.Processors.MANUFACTORY;
import static igentuman.nc.setup.entries.Processors.PRESSURIZER;

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
            saves.add(save(output, "exdeorum", MANUFACTORY, material + "_ore_chunk",
                    List.of(item("exdeorum:" + material + "_ore_chunk", 1)), tag("c:dusts/" + material, 1)));
        }

        saves.add(save(output, "ae2", MANUFACTORY, "certus_quartz_dust",
                List.of(tag("c:gems/certus_quartz", 1)), tag("c:dusts/certus_quartz", 1)));
        saves.add(save(output, "ae2", MANUFACTORY, "fluix_dust",
                List.of(tag("c:gems/fluix", 1)), tag("c:dusts/fluix", 1)));
        for (String processor : List.of("logic", "calculation", "engineering")) {
            saves.add(save(output, "ae2", ASSEMBLER, processor + "_processor",
                    List.of(item("ae2:printed_" + processor + "_processor", 1),
                            item("ae2:printed_silicon", 1), item("minecraft:redstone", 1)),
                    item("ae2:" + processor + "_processor", 1)));
        }
        saves.add(save(output, "ae2", ASSEMBLER, "formation_core",
                List.of(tag("ae2:all_certus_quartz", 1), tag("c:dusts/fluix", 1),
                        item("ae2:logic_processor", 1)), item("ae2:formation_core", 2)));
        saves.add(save(output, "ae2", ASSEMBLER, "annihilation_core",
                List.of(tag("ae2:all_nether_quartz", 1), tag("c:dusts/fluix", 1),
                        item("ae2:logic_processor", 1)), item("ae2:annihilation_core", 2)));

        saves.add(save(output, "create", ALLOY_SMELTER, "brass_ingot",
                List.of(tag("c:ingots/copper", 1), tag("c:ingots/zinc", 1)), tag("c:ingots/brass", 2),
                300, ModRecipeProvider.ENERGY + 5));
        saves.add(save(output, "create", ALLOY_SMELTER, "andesite_alloy",
                List.of(item("minecraft:andesite", 1), tag("c:nuggets/iron", 1)), item("create:andesite_alloy", 1),
                300, ModRecipeProvider.ENERGY + 5));
        saves.add(save(output, "create", PRESSURIZER, "brass_sheet",
                List.of(tag("c:ingots/brass", 1)), tag("c:plates/brass", 1)));
        saves.add(save(output, "create", ASSEMBLER, "rose_quartz",
                List.of(tag("c:gems/quartz", 1), tag("c:dusts/redstone", 8)), item("create:rose_quartz", 1)));
        saves.add(save(output, "create", ASSEMBLER, "electron_tube",
                List.of(item("create:polished_rose_quartz", 1), tag("c:plates/iron", 1)), item("create:electron_tube", 1)));

        saves.add(save(output, "immersiveengineering", MANUFACTORY, "coke_dust",
                List.of(tag("c:coal_coke", 1)), tag("c:dusts/coal_coke", 1)));
        for (String material : List.of("gold", "nickel", "constantan")) {
            saves.add(save(output, "immersiveengineering", PRESSURIZER, material + "_plate",
                    List.of(tag("c:ingots/" + material, 1)), tag("c:plates/" + material, 1)));
        }
        saves.add(save(output, "immersiveengineering", ALLOY_SMELTER, "constantan_ingot",
                List.of(tag("c:ingots/copper", 1), tag("c:ingots/nickel", 1)), tag("c:ingots/constantan", 2),
                300, ModRecipeProvider.ENERGY + 5));
        for (String material : List.of("iron", "steel")) {
            saves.add(save(output, "immersiveengineering", ASSEMBLER, "component_" + material,
                    List.of(tag("c:plates/" + material, 4), tag("c:ingots/copper", 1)),
                    item("immersiveengineering:component_" + material, 1)));
        }
        saves.add(save(output, "immersiveengineering", ASSEMBLER, "electron_tube",
                List.of(tag("c:glass_blocks", 1), tag("c:plates/nickel", 1),
                        tag("c:wires/copper", 1), tag("c:dusts/redstone", 1)),
                item("immersiveengineering:electron_tube", 3), 250, 200));
        saves.add(save(output, "immersiveengineering", ASSEMBLER, "component_electronic",
                List.of(tag("immersiveengineering:treated_wood_slab", 1), tag("c:gems/quartz", 1),
                        tag("c:dusts/redstone", 1), tag("c:wires/electrum", 1)),
                item("immersiveengineering:component_electronic", 1), 250, 200));
        saves.add(save(output, "immersiveengineering", ASSEMBLER, "component_electronic_adv",
                List.of(tag("c:plates/plastic", 1), item("immersiveengineering:electron_tube", 2),
                        tag("c:wires/aluminum", 1)),
                item("immersiveengineering:component_electronic_adv", 1), 250, 200));
        saves.add(save(output, "immersiveengineering", ASSEMBLER, "circuit_board",
                List.of(tag("c:plates/plastic", 1), tag("c:plates/copper", 1)),
                item("immersiveengineering:circuit_board", 1), 250, 200));
        saves.add(save(output, "immersiveengineering", ASSEMBLER, "light_bulb_paper",
                List.of(tag("c:glass_blocks", 1), tag("c:paper", 3), tag("c:ingots/copper", 1)),
                item("immersiveengineering:light_bulb", 3), 250, 200));
        saves.add(save(output, "immersiveengineering", ASSEMBLER, "light_bulb_bamboo",
                List.of(tag("c:glass_blocks", 1), item("minecraft:bamboo", 3), tag("c:ingots/copper", 1)),
                item("immersiveengineering:light_bulb", 3), 250, 200));

        saves.add(save(output, "mekanism", PRESSURIZER, "hdpe_sheet",
                List.of(item("mekanism:hdpe_pellet", 3)), item("mekanism:hdpe_sheet", 1), 200, 200));
        saves.add(save(output, "mekanism", ALLOY_SMELTER, "infused_alloy",
                List.of(tag("c:ingots/copper", 8), tag("mekanism:enriched/redstone", 1)),
                tag("c:alloys/advanced", 8), 1600, 200));
        saves.add(save(output, "mekanism", ALLOY_SMELTER, "reinforced_alloy",
                List.of(tag("c:alloys/advanced", 4), tag("mekanism:enriched/diamond", 1)),
                tag("c:alloys/elite", 4), 800, 200));
        saves.add(save(output, "mekanism", ALLOY_SMELTER, "atomic_alloy",
                List.of(tag("c:alloys/elite", 2), tag("mekanism:enriched/obsidian", 1)),
                tag("c:alloys/ultimate", 2), 400, 200));
        saves.add(save(output, "mekanism", ALLOY_SMELTER, "refined_obsidian_dust",
                List.of(tag("c:dusts/obsidian", 8), tag("mekanism:enriched/diamond", 1)),
                tag("c:dusts/refined_obsidian", 8), 1600, 200));
        saves.add(save(output, "mekanism", ALLOY_SMELTER, "basic_control_circuit",
                List.of(tag("c:ingots/osmium", 4), tag("mekanism:enriched/redstone", 1)),
                tag("c:circuits/basic", 4), 800, 200));
        saves.add(save(output, "mekanismgenerators", ALLOY_SMELTER, "hohlraum",
                List.of(tag("c:dusts/gold", 32), tag("mekanism:enriched/carbon", 1)),
                item("mekanismgenerators:hohlraum", 8), 1600, 200, "mekanism"));
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    private static JsonObject item(String id, int count) {
        return reference("item", id, count);
    }

    private static JsonObject tag(String id, int count) {
        return reference("tag", id, count);
    }

    private static JsonObject reference(String key, String id, int count) {
        ResourceLocation.parse(id);
        JsonObject reference = new JsonObject();
        reference.addProperty(key, id);
        reference.addProperty("count", count);
        return reference;
    }

    private CompletableFuture<?> save(CachedOutput output, String modId, String processor, String name,
                                      List<JsonObject> itemInputs, JsonObject result) {
        return save(output, modId, processor, name, itemInputs, result,
                ModRecipeProvider.TIME, ModRecipeProvider.ENERGY + (processor.equals(ASSEMBLER) ? 25 : 0));
    }

    private CompletableFuture<?> save(CachedOutput output, String modId, String processor, String name,
                                      List<JsonObject> itemInputs, JsonObject result, int processTime, int energyPerTick,
                                      String... additionalMods) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", rl(processor).toString());
        recipe.addProperty("process_time", processTime);
        recipe.addProperty("energy_per_tick", energyPerTick);

        JsonArray conditions = new JsonArray();
        conditions.add(modLoadedCondition(modId));
        for (String additionalMod : additionalMods) {
            conditions.add(modLoadedCondition(additionalMod));
        }
        JsonArray inputs = new JsonArray();
        for (JsonObject input : itemInputs) {
            inputs.add(input);
            conditions.add(contentCondition(input));
        }
        recipe.add("item_inputs", inputs);

        JsonArray results = new JsonArray();
        results.add(result);
        recipe.add("item_outputs", results);
        conditions.add(contentCondition(result));
        recipe.add("neoforge:conditions", conditions);

        return DataProvider.saveStable(output, recipe, recipes.json(rl(processor + "/" + modId + "/" + name)));
    }

    private static JsonObject modLoadedCondition(String modId) {
        JsonObject modLoaded = new JsonObject();
        modLoaded.addProperty("type", "neoforge:mod_loaded");
        modLoaded.addProperty("modid", modId);
        return modLoaded;
    }

    private static JsonObject contentCondition(JsonObject reference) {
        if (reference.has("item")) {
            JsonObject itemExists = new JsonObject();
            itemExists.addProperty("type", "neoforge:item_exists");
            itemExists.addProperty("item", reference.get("item").getAsString());
            return itemExists;
        }
        JsonObject tagEmpty = new JsonObject();
        tagEmpty.addProperty("type", "neoforge:tag_empty");
        tagEmpty.addProperty("tag", reference.get("tag").getAsString());
        JsonObject tagPresent = new JsonObject();
        tagPresent.addProperty("type", "neoforge:not");
        tagPresent.add("value", tagEmpty);
        return tagPresent;
    }

    @Override
    public String getName() {
        return "NuclearCraft optional processor recipes";
    }
}
