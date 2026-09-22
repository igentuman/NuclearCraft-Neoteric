package igentuman.nc.multiblock.accelerator;

import igentuman.nc.api.particle.ParticleAction;
import igentuman.nc.particle.ParticleStorage;
import net.minecraft.resources.ResourceLocation;

public record AcceleratorRuntimeState(
        ParticleStorage input,
        ParticleStorage pendingOutput,
        long storedHeat,
        int controlSignal,
        int overheatCooldown,
        ResourceLocation activeCoolantRecipeId
) {

    public void tickBeam(Runnable tick) {
        pendingOutput.extract(0, Long.MAX_VALUE, ParticleAction.EXECUTE);
        try {
            tick.run();
        } finally {
            clearBeams();
        }
    }

    public void clearBeams() {
        input.extract(0, Long.MAX_VALUE, ParticleAction.EXECUTE);
        pendingOutput.extract(0, Long.MAX_VALUE, ParticleAction.EXECUTE);
    }
}
