package igentuman.nc.api.particle;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;

import static igentuman.nc.NuclearCraft.rl;

public final class ParticleCapabilities {

    public static final BlockCapability<IParticleHandler, Direction> BLOCK =
            BlockCapability.createSided(rl("particle_handler"), IParticleHandler.class);

    private ParticleCapabilities() {
    }
}
