package igentuman.nc.setup.registration;

import igentuman.nc.client.particle.FusionBeamParticleData;
import igentuman.nc.client.particle.FusionBeamParticleType;
import igentuman.nc.setup.registry.ParticleTypeDeferredRegister;
import igentuman.nc.setup.registry.ParticleTypeRegistryObject;
import net.minecraft.particles.BasicParticleType;

import static igentuman.nc.NuclearCraft.MODID;

public class NcParticleTypes {

    private NcParticleTypes() {
    }
    public static final ParticleTypeDeferredRegister PARTICLE_TYPES = new ParticleTypeDeferredRegister(MODID);

    public static final ParticleTypeRegistryObject<BasicParticleType, BasicParticleType> RADIATION =
            PARTICLE_TYPES.registerBasicParticle("radiation");

    public static final ParticleTypeRegistryObject<FusionBeamParticleData, FusionBeamParticleType> FUSION_BEAM =
            PARTICLE_TYPES.register("fusion_beam", FusionBeamParticleType::new);


}