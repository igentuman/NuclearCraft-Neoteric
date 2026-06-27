package igentuman.nc.config;

import igentuman.nc.registration.MaterialEntry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Materials {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final Map<String, Map<String, ModConfigSpec.BooleanValue>> MATERIAL_TYPES = new LinkedHashMap<>();

    public static final ModConfigSpec SPEC;

    static {
        ModEntries.ENTRIES.values().stream()
                .filter(e -> e.materialEntry() != null)
                .sorted(Comparator.comparing(ModEntry::name))
                .forEach(entry -> {
                    MaterialEntry mat = entry.materialEntry();
                    BUILDER.push(mat.name);
                    Map<String, ModConfigSpec.BooleanValue> types = new LinkedHashMap<>();

                    if (mat.hasOre())    types.put("ore",     BUILDER.define("ore", true));
                    if (mat.hasRawOre()) types.put("raw_ore", BUILDER.define("raw_ore", true));
                    if (mat.hasIngot())  types.put("ingot",   BUILDER.define("ingot", true));
                    if (mat.hasGem())    types.put("gem",     BUILDER.define("gem", true));
                    if (mat.hasDust())   types.put("dust",    BUILDER.define("dust", true));
                    if (mat.hasPlate())  types.put("plate",   BUILDER.define("plate", true));
                    if (mat.hasNugget()) types.put("nugget",  BUILDER.define("nugget", true));
                    if (mat.hasBlock())  types.put("block",   BUILDER.define("block", true));
                    if (mat.hasFluid())  types.put("fluid",   BUILDER.define("fluid", true));

                    MATERIAL_TYPES.put(mat.name, types);
                    BUILDER.pop();
                });

        SPEC = BUILDER.build();
    }

    public static boolean isTypeEnabled(String materialName, String type) {
        Map<String, ModConfigSpec.BooleanValue> types = MATERIAL_TYPES.get(materialName);
        if (types == null) return true;
        ModConfigSpec.BooleanValue val = types.get(type);
        return val == null || val.get();
    }
}
