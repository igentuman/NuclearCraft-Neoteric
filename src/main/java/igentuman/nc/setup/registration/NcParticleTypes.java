package igentuman.nc.setup.registration;

import igentuman.nc.NuclearCraft;
import igentuman.nc.client.particle.FusionBeamParticleData;
import igentuman.nc.client.particle.FusionBeamParticleType;
import igentuman.nc.registry.ParticleTypeDeferredRegister;
import igentuman.nc.registry.ParticleTypeRegistryObject;
import net.minecraft.core.particles.SimpleParticleType;

public class NcParticleTypes {

    private NcParticleTypes() {
    }

    public static final ParticleTypeDeferredRegister PARTICLE_TYPES = new ParticleTypeDeferredRegister(NuclearCraft.MODID);

    public static final ParticleTypeRegistryObject<SimpleParticleType, SimpleParticleType> RADIATION = PARTICLE_TYPES.registerBasicParticle("radiation");
    public static final ParticleTypeRegistryObject<FusionBeamParticleData, FusionBeamParticleType> FUSION_BEAM = PARTICLE_TYPES.register("fusion_beam", FusionBeamParticleType::new);
}