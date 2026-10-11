package igentuman.nc.config;

import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Processors {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue TIME_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue ENERGY_MULTIPLIER;
    public static final Map<String, Modifiers> MODIFIERS = new LinkedHashMap<>();

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("common");
        TIME_MULTIPLIER = BUILDER
                .comment("Multiplier applied to the process time of every processor recipe")
                .defineInRange("time_multiplier", 1.0D, 0.01D, 100.0D);
        ENERGY_MULTIPLIER = BUILDER
                .comment("Multiplier applied to the energy per tick of every processor recipe")
                .defineInRange("energy_multiplier", 1.0D, 0.0D, 100.0D);
        BUILDER.pop();

        ModEntries.ENTRIES.values().stream()
                .filter(Processors::isProcessor)
                .sorted(Comparator.comparing(ModEntry::name))
                .forEach(entry -> {
                    BUILDER.push(entry.name());
                    ModConfigSpec.DoubleValue time = BUILDER
                            .comment("Process time multiplier, stacks with the common multiplier")
                            .defineInRange("time_multiplier", 1.0D, 0.01D, 100.0D);
                    ModConfigSpec.DoubleValue energy = BUILDER
                            .comment("Energy per tick multiplier, stacks with the common multiplier")
                            .defineInRange("energy_multiplier", 1.0D, 0.0D, 100.0D);
                    BUILDER.pop();
                    MODIFIERS.put(entry.name(), new Modifiers(time, energy));
                });

        SPEC = BUILDER.build();
    }

    private static boolean isProcessor(ModEntry entry) {
        return entry.recipeFamily() != null && entry.menu() != null && entry.materialEntry() == null;
    }

    public static boolean isEnabled(String name) {
        return Entries.isEnabled(name);
    }

    public static int processTime(String processor, int base) {
        if (!SPEC.isLoaded()) return base;
        Modifiers modifiers = MODIFIERS.get(processor);
        double multiplier = TIME_MULTIPLIER.get() * (modifiers == null ? 1.0D : modifiers.time().get());
        return Math.max(1, (int) Math.round(base * multiplier));
    }

    public static int energyPerTick(String processor, int base) {
        if (!SPEC.isLoaded()) return base;
        Modifiers modifiers = MODIFIERS.get(processor);
        double multiplier = ENERGY_MULTIPLIER.get() * (modifiers == null ? 1.0D : modifiers.energy().get());
        return (int) Math.round(base * multiplier);
    }

    public record Modifiers(ModConfigSpec.DoubleValue time, ModConfigSpec.DoubleValue energy) {}
}
