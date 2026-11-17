package igentuman.nc.compat.kubejs;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.client.LangKubeEvent;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import igentuman.nc.registration.FissionFuelEntry;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

/** Runtime assets for fuels which cannot have models or language entries produced by datagen. */
final class KubeJSFuelAssets {
    private KubeJSFuelAssets() {}

    static void generateAssets(KubeAssetGenerator generator, FissionFuelEntry fuel) {
        fuel.fuelItems().forEach((variant, item) -> itemModel(generator, item.getId(), fuel, variant, false));
        fuel.depletedItems().forEach((variant, item) -> itemModel(generator, item.getId(), fuel, variant, true));
        for (var material : fuel.fluids()) {
            var fluid = material.materialFluid();
            JsonObject bucket = new JsonObject();
            bucket.addProperty("parent", "neoforge:item/bucket");
            bucket.addProperty("loader", "neoforge:fluid_container");
            bucket.addProperty("fluid", fluid.source().getId().toString());
            bucket.addProperty("apply_tint", true);
            bucket.addProperty("flip_gas", false);
            jsonIfMissing(generator, "models/item/" + fluid.bucket().getId().getPath(), bucket);

            String block = fluid.fluidBlock().getId().getPath();
            JsonObject model = new JsonObject();
            JsonObject textures = new JsonObject();
            textures.addProperty("particle", MODID + ":block/fluid/molten_still");
            model.add("textures", textures);
            jsonIfMissing(generator, "models/block/" + block, model);
            JsonObject state = new JsonObject();
            JsonObject variants = new JsonObject();
            JsonObject variant = new JsonObject();
            variant.addProperty("model", MODID + ":block/" + block);
            variants.add("", variant);
            state.add("variants", variants);
            jsonIfMissing(generator, "blockstates/" + block, state);
        }
    }

    private static void itemModel(KubeAssetGenerator generator, ResourceLocation id,
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
    }

    private static void jsonIfMissing(KubeAssetGenerator generator, String path, JsonObject json) {
        if (!hasResource(path + ".json")) {
            generator.json(rl(path), json);
        }
    }

    private static boolean hasResource(String path) {
        return KubeJSFuelAssets.class.getClassLoader().getResource("assets/" + MODID + "/" + path) != null;
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
