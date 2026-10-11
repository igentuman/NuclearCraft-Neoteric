package igentuman.nc.particle;

import igentuman.nc.setup.Registers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static igentuman.nc.NuclearCraft.rl;

public final class ParticleSourceCatalog {

    public static final long DEFAULT_ENERGY_KEV = 0;
    public static final double DEFAULT_FOCUS = 0.4;
    public static final long FLUID_PARTICLES_PER_MILLIBUCKET = 10_000;

    private static final Map<ResourceLocation, ParticleSourceData> ITEM_SOURCES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, ParticleSourceData> FLUID_SOURCES = new LinkedHashMap<>();

    static {
        registerItemSource(rl("source_calcium_48"), charge(rl("calcium_48_ion"), 5_000_000));
        registerItemSource(rl("source_iridium_192"), charge(rl("positron"), 1_000_000));
        registerItemSource(rl("tungsten_filament"), charge(rl("electron"), 50_000_000));
        registerItemSource(rl("antideuterium"), charge(rl("antideuteron"), 50_000_000));
        registerItemSource(rl("antihelium"), charge(rl("antialpha"), 50_000_000));
        registerItemSource(rl("antihelium3"), charge(rl("antihelion"), 50_000_000));
        registerItemSource(rl("antihydrogen"), charge(rl("antiproton"), 50_000_000));
        registerItemSource(rl("antitritium"), charge(rl("antitriton"), 50_000_000));

        registerFluidSource(rl("diborane"), charge(rl("boron_ion"), FLUID_PARTICLES_PER_MILLIBUCKET));
        registerFluidSource(rl("hydrogen"), charge(rl("proton"), FLUID_PARTICLES_PER_MILLIBUCKET));
        registerFluidSource(rl("deuterium"), charge(rl("deuteron"), FLUID_PARTICLES_PER_MILLIBUCKET));
        registerFluidSource(rl("tritium"), charge(rl("triton"), FLUID_PARTICLES_PER_MILLIBUCKET));
        registerFluidSource(rl("helium"), charge(rl("alpha"), FLUID_PARTICLES_PER_MILLIBUCKET));
    }

    private ParticleSourceCatalog() {}

    public static Map<ResourceLocation, ParticleSourceData> itemSources() {
        return Collections.unmodifiableMap(ITEM_SOURCES);
    }

    public static Map<ResourceLocation, ParticleSourceData> fluidSources() {
        return Collections.unmodifiableMap(FLUID_SOURCES);
    }

    public static synchronized void registerItemSource(ResourceLocation itemId, ParticleSourceData data) {
        ITEM_SOURCES.put(itemId, data);
    }

    public static synchronized void registerFluidSource(ResourceLocation fluidId, ParticleSourceData data) {
        FLUID_SOURCES.put(fluidId, data);
    }

    public static synchronized void removeItemSource(ResourceLocation itemId) {
        ITEM_SOURCES.remove(itemId);
    }

    public static synchronized void removeFluidSource(ResourceLocation fluidId) {
        FLUID_SOURCES.remove(fluidId);
    }

    @Nullable
    public static ParticleSourceData itemSource(ItemStack stack) {
        return stack.isEmpty() ? null : ITEM_SOURCES.get(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    @Nullable
    public static ParticleSourceData sourceData(ItemStack stack) {
        ParticleSourceData template = itemSource(stack);
        if (template == null) return null;
        ParticleSourceData stored = stack.get(Registers.PARTICLE_SOURCE.get());
        return stored != null ? stored : template;
    }

    @Nullable
    public static ParticleSourceData fluidSource(FluidStack stack) {
        return stack.isEmpty() ? null : FLUID_SOURCES.get(BuiltInRegistries.FLUID.getKey(stack.getFluid()));
    }

    public static ParticleSourceData charge(ResourceLocation particleId, long capacity) {
        return ParticleSourceData.fullCharge(particleId, capacity, DEFAULT_ENERGY_KEV, DEFAULT_FOCUS);
    }
}
