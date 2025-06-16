package igentuman.nc.content.particles;

import net.minecraft.world.item.ItemStack;

import java.util.HashMap;

import static igentuman.nc.content.particles.Particles.*;

public class ParticleSources {

    public static final int moleAmount = 1000000;

    public final static HashMap<String, ParticleStack> sources = new HashMap<>();

    public static void init()
    {
        sources.put("source_calcium_48", new ParticleStack(calcium_48_ion, 50 * moleAmount, 0,0));
        sources.put("source_sodium_22", new ParticleStack(positron, 50 * moleAmount, 0,0));
        sources.put("source_cobalt_60", new ParticleStack(positron, 10 * moleAmount, 0,0));
        sources.put("source_iridium_192", new ParticleStack(positron, 10 * moleAmount, 0,0));
        sources.put("tungsten_filament", new ParticleStack(photon, 50 * moleAmount, 0,0));
    }

    public static int getCapacity(ItemStack stack) {
        if (sources.containsKey(stack.getItem().toString())) {
            return sources.get(stack.getItem().toString()).getAmount();
        }
        return 0;
    }
}
