package igentuman.nc.recipe.particle;

import java.util.Map;

public final class LegacyParticleRecipeAdapter {

    public static final Map<String, String> FIELD_ALIASES = Map.ofEntries(
            Map.entry("input", "item_inputs"),
            Map.entry("inputFluids", "fluid_inputs"),
            Map.entry("output", "item_outputs"),
            Map.entry("outputFluids", "fluid_outputs"),
            Map.entry("inputParticles", "particle_inputs"),
            Map.entry("outputParticles", "particle_outputs"),
            Map.entry("meanEnergy", "mean_energy_kev"),
            Map.entry("minEnergy", "minimum_energy_kev"),
            Map.entry("maxEnergy", "maximum_energy_kev"),
            Map.entry("crossSection", "cross_section"),
            Map.entry("energyReleased", "released_energy_kev"),
            Map.entry("coolingRate", "heat_per_mb")
    );

    private LegacyParticleRecipeAdapter() {
    }
}
