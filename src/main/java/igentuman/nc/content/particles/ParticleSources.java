package igentuman.nc.content.particles;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashMap;

import static igentuman.nc.content.particles.Particles.*;

public class ParticleSources {

    public static final int moleAmount = 1000000;

    public final static HashMap<String, ParticleStack> sources = new HashMap<>();
    public final static HashMap<String, ParticleStack> fluidSources = new HashMap<>();

    public static void init()
    {
        sources.put("source_calcium_48", new ParticleStack(calcium_48_ion, 5 * moleAmount, 0,0));
        sources.put("source_iridium_192", new ParticleStack(positron, 1 * moleAmount, 0,0));
        sources.put("tungsten_filament", new ParticleStack(photon, 50 * moleAmount, 0,0));
        sources.put("antideuterium", new ParticleStack(antideuteron, 50 * moleAmount, 0,0));
        sources.put("antihelium", new ParticleStack(antialpha, 50 * moleAmount, 0,0));
        sources.put("antihelium3", new ParticleStack(antihelion, 50 * moleAmount, 0,0));
        sources.put("antihydrogen", new ParticleStack(antiproton, 50 * moleAmount, 0,0));
        sources.put("antitritium", new ParticleStack(antitriton, 50 * moleAmount, 0,0));
        sources.put("empty", new ParticleStack());
        sources.put("glueballs", new ParticleStack());
        sources.put("positronium", new ParticleStack());
        sources.put("tauonium", new ParticleStack());
        fluidSources.put("diborane", new ParticleStack(boron_ion, 50 * moleAmount, 0,0));
        fluidSources.put("hydrogen", new ParticleStack(proton, 50 * moleAmount, 0,0));
        fluidSources.put("deuterium", new ParticleStack(deuteron, 50 * moleAmount, 0,0));
        fluidSources.put("tritium", new ParticleStack(triton, 50 * moleAmount, 0,0));
        fluidSources.put("helium", new ParticleStack(alpha, 50 * moleAmount, 0,0));
    }

    public static int getCapacity(ItemStack stack) {
        if (sources.containsKey(stack.getItem().toString())) {
            return sources.get(stack.getItem().toString()).getAmount();
        }
        return 0;
    }

    public static ParticleStack getParticleFromFluid(FluidStack fluidStack) {
        if (fluidStack == null || fluidStack.isEmpty()) {
            return null;
        }
        String fluidName = fluidStack.getFluid().builtInRegistryHolder().key().location().getPath();
        if (fluidSources.containsKey(fluidName)) {
            return fluidSources.get(fluidName).copy();
        }
        return null;
    }
}
