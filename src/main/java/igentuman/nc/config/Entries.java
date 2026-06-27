package igentuman.nc.config;

import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Universal per-entry enable toggle for every non-material {@link ModEntry}
 * (items, tools, armor, plain blocks, block entities, multiblock controllers/ports).
 * Material entries are gated per product type in {@link Materials} instead.
 * Disabling an entry makes its runtime behavior inert and hides it from creative/JEI/EMI;
 * the underlying block/item stays registered (see ModEntry.isEnabled).
 */
public class Entries {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final Map<String, ModConfigSpec.BooleanValue> ENABLED = new LinkedHashMap<>();

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("entries");
        ModEntries.ENTRIES.values().stream()
                .filter(e -> e.materialEntry() == null)
                .sorted(Comparator.comparing(ModEntry::name))
                .forEach(entry ->
                        ENABLED.put(entry.name(), BUILDER.define(entry.name(), true))
                );
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static boolean isEnabled(String name) {
        ModConfigSpec.BooleanValue val = ENABLED.get(name);
        return val == null || val.get();
    }
}
