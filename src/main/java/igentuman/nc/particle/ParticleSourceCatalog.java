package igentuman.nc.particle;

import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

import static igentuman.nc.NuclearCraft.rl;

public final class ParticleSourceCatalog {

    private static final long BUILTIN_ENERGY_KEV = 0;
    private static final double BUILTIN_FOCUS = 0.4;
    private static final long FLUID_PARTICLES_PER_MILLIBUCKET = 10_000;
    private static final ParticleSourceCatalog BUILTIN = createBuiltin();

    private final Map<ResourceLocation, ParticleSourceData> itemSources;
    private final Map<ResourceLocation, ParticleSourceData> fluidSources;

    public ParticleSourceCatalog(
            Map<ResourceLocation, ParticleSourceData> itemSources,
            Map<ResourceLocation, ParticleSourceData> fluidSources
    ) {
        this.itemSources = Map.copyOf(itemSources);
        this.fluidSources = Map.copyOf(fluidSources);
    }

    public Map<ResourceLocation, ParticleSourceData> itemSources() {
        return itemSources;
    }

    public Map<ResourceLocation, ParticleSourceData> fluidSources() {
        return fluidSources;
    }

    public static ParticleSourceCatalog builtin() {
        return BUILTIN;
    }

    private static ParticleSourceCatalog createBuiltin() {
        Map<ResourceLocation, ParticleSourceData> items = new LinkedHashMap<>();
        items.put(rl("source_calcium_48"), builtinCharge(rl("calcium_48_ion"), 5_000_000));
        items.put(rl("source_iridium_192"), builtinCharge(rl("positron"), 1_000_000));
        items.put(rl("tungsten_filament"), builtinCharge(rl("electron"), 50_000_000));
        items.put(rl("antideuterium"), builtinCharge(rl("antideuteron"), 50_000_000));
        items.put(rl("antihelium"), builtinCharge(rl("antialpha"), 50_000_000));
        items.put(rl("antihelium3"), builtinCharge(rl("antihelion"), 50_000_000));
        items.put(rl("antihydrogen"), builtinCharge(rl("antiproton"), 50_000_000));
        items.put(rl("antitritium"), builtinCharge(rl("antitriton"), 50_000_000));

        Map<ResourceLocation, ParticleSourceData> fluids = new LinkedHashMap<>();
        fluids.put(rl("diborane"), builtinCharge(rl("boron_ion"), FLUID_PARTICLES_PER_MILLIBUCKET));
        fluids.put(rl("hydrogen"), builtinCharge(rl("proton"), FLUID_PARTICLES_PER_MILLIBUCKET));
        fluids.put(rl("deuterium"), builtinCharge(rl("deuteron"), FLUID_PARTICLES_PER_MILLIBUCKET));
        fluids.put(rl("tritium"), builtinCharge(rl("triton"), FLUID_PARTICLES_PER_MILLIBUCKET));
        fluids.put(rl("helium"), builtinCharge(rl("alpha"), FLUID_PARTICLES_PER_MILLIBUCKET));

        return new ParticleSourceCatalog(items, fluids);
    }

    private static ParticleSourceData builtinCharge(ResourceLocation particleId, long capacity) {
        return ParticleSourceData.fullCharge(particleId, capacity, BUILTIN_ENERGY_KEV, BUILTIN_FOCUS);
    }
}
