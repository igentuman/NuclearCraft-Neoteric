package igentuman.nc.compat.kubejs;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.client.LangKubeEvent;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.registration.HeatSinkEntry;
import net.minecraft.resources.Identifier;

import java.util.Locale;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

/** Supplies fallback assets and English names for script-defined fuels and heat sinks. */
final class KubeJSContentAssets {
    private KubeJSContentAssets() {}

    static void generateAssets(KubeAssetGenerator generator, FissionFuelEntry fuel) {
        fuel.fuelItems().forEach((variant, item) -> itemModel(generator, item.getId(), fuel, variant, false));
        fuel.depletedItems().forEach((variant, item) -> itemModel(generator, item.getId(), fuel, variant, true));
        for (var material : fuel.fluids()) {
            var fluid = material.materialFluid();
            JsonObject bucketTextures = new JsonObject();
            bucketTextures.addProperty("base", "minecraft:item/bucket");
            bucketTextures.addProperty("fluid", "neoforge:item/mask/bucket_fluid");
            JsonObject bucket = new JsonObject();
            bucket.addProperty("type", "neoforge:fluid_container");
            bucket.addProperty("fluid", fluid.source().getId().toString());
            bucket.add("textures", bucketTextures);
            itemDefinition(generator, fluid.bucket().getId(), bucket);

            JsonObject model = new JsonObject();
            JsonObject textures = new JsonObject();
            textures.addProperty("particle", MODID + ":block/fluid/molten_still");
            model.add("textures", textures);
            blockModel(generator, fluid.fluidBlock().getId(), model);
        }
    }

    static void generateAssets(KubeAssetGenerator generator, HeatSinkEntry sink) {
        Identifier id = sink.block().getId();
        String texture = "block/heat_sink/" + sink.name;
        if (!hasResource("textures/" + texture + ".png")) {
            texture = "block/heat_sink/empty";
        }
        JsonObject textures = new JsonObject();
        textures.addProperty("all", rl(texture).toString());
        JsonObject model = new JsonObject();
        model.addProperty("parent", "minecraft:block/cube_all");
        model.add("textures", textures);
        blockModel(generator, id, model);
        itemDefinition(generator, id, modelReference(id.withPrefix("block/")));
    }

    private static void blockModel(KubeAssetGenerator generator, Identifier id, JsonObject model) {
        Identifier modelId = id.withPrefix("block/");
        jsonIfMissing(generator, "models/" + modelId.getPath(), model);

        JsonObject variant = new JsonObject();
        variant.addProperty("model", modelId.toString());
        JsonObject variants = new JsonObject();
        variants.add("", variant);
        JsonObject state = new JsonObject();
        state.add("variants", variants);
        jsonIfMissing(generator, "blockstates/" + id.getPath(), state);
    }

    private static void itemModel(KubeAssetGenerator generator, Identifier id,
                                  FissionFuelEntry fuel, String variant, boolean depleted) {
        String folder = depleted ? "depleted/" : "";
        String texture = "item/fuel/" + fuel.group + "/" + folder + fuel.name.replace('-', '_') + variant;
        if (!hasResource("textures/" + texture + ".png")) {
            texture = "item/fuel/uranium/" + folder + "leu_235" + variant;
        }
        JsonObject model = new JsonObject();
        model.addProperty("parent", "minecraft:item/generated");
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", MODID + ":" + texture);
        model.add("textures", textures);
        jsonIfMissing(generator, "models/item/" + id.getPath(), model);
        itemDefinition(generator, id, modelReference(id.withPrefix("item/")));
    }

    private static JsonObject modelReference(Identifier modelId) {
        JsonObject model = new JsonObject();
        model.addProperty("type", "minecraft:model");
        model.addProperty("model", modelId.toString());
        return model;
    }

    private static void itemDefinition(KubeAssetGenerator generator, Identifier id, JsonObject model) {
        JsonObject definition = new JsonObject();
        definition.add("model", model);
        jsonIfMissing(generator, "items/" + id.getPath(), definition);
    }

    private static void jsonIfMissing(KubeAssetGenerator generator, String path, JsonObject json) {
        if (!hasResource(path + ".json")) {
            generator.json(rl(path), json);
        }
    }

    private static boolean hasResource(String path) {
        return KubeJSContentAssets.class.getClassLoader().getResource("assets/" + MODID + "/" + path) != null;
    }

    static void generateLang(LangKubeEvent event, FissionFuelEntry fuel) {
        fuel.fuelItems().forEach((variant, item) ->
                event.add(MODID, item.get().getDescriptionId(), displayName(fuel, variant, false)));
        fuel.depletedItems().forEach((variant, item) ->
                event.add(MODID, item.get().getDescriptionId(), displayName(fuel, variant, true)));
        for (var material : fuel.fluids()) {
            var fluid = material.materialFluid();
            String name = readableName(fluid.source().getId().getPath());
            event.add(MODID, fluid.fluidType().get().getDescriptionId(), name);
            event.add(MODID, fluid.bucket().get().getDescriptionId(), name + " Bucket");
            event.add(MODID, fluid.fluidBlock().get().getDescriptionId(), name);
        }
    }

    static void generateLang(LangKubeEvent event, HeatSinkEntry sink) {
        event.add(MODID, sink.block().get().getDescriptionId(), readableName(sink.name) + " Heat Sink");
    }

    private static String displayName(FissionFuelEntry fuel, String variant, boolean depleted) {
        return (depleted ? "Depleted " : "Fuel ") + readableName(fuel.group) + " "
                + fuel.name.toUpperCase(Locale.ROOT)
                + (variant.isEmpty() ? "" : " " + variant.substring(1).toUpperCase(Locale.ROOT));
    }

    private static String readableName(String id) {
        StringBuilder name = new StringBuilder();
        for (String word : id.split("_")) {
            if (word.isEmpty()) continue;
            if (!name.isEmpty()) name.append(' ');
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return name.toString();
    }
}
