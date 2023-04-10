package igentuman.nc.setup.registration;

import igentuman.nc.NuclearCraft;
import net.minecraft.core.particles.SimpleParticleType;

public class NcParticleTypes {

    private NcParticleTypes() {
    }

    public static final ParticleTypeDeferredRegister PARTICLE_TYPES = new ParticleTypeDeferredRegister(NuclearCraft.MODID);

    public static final ParticleTypeRegistryObject<SimpleParticleType, SimpleParticleType> RADIATION = PARTICLE_TYPES.registerBasicParticle("radiation");
}