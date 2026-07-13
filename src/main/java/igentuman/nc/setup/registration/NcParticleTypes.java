package igentuman.nc.setup.registration;

import igentuman.nc.client.particle.FusionBeamParticleData;
import igentuman.nc.client.particle.FusionBeamParticleType;
import igentuman.nc.registry.ParticleTypeRegistryObject;
import net.minecraft.core.particles.SimpleParticleType;

import static igentuman.nc.setup.registration.Registries.PARTICLE_TYPES;

public class NcParticleTypes {

    private NcParticleTypes() {
    }
    public static final ParticleTypeRegistryObject<SimpleParticleType, SimpleParticleType> RADIATION = PARTICLE_TYPES.registerBasicParticle("radiation");
    public static final ParticleTypeRegistryObject<FusionBeamParticleData, FusionBeamParticleType> FUSION_BEAM = PARTICLE_TYPES.register("fusion_beam", FusionBeamParticleType::new);
    public static final ParticleTypeRegistryObject<SimpleParticleType, SimpleParticleType> FIRE_VERTICAL = PARTICLE_TYPES.registerBasicParticle("fire_vertical");
    public static final ParticleTypeRegistryObject<SimpleParticleType, SimpleParticleType> FIRE_VERTICAL1 = PARTICLE_TYPES.registerBasicParticle("fire_vertical1");
    public static final ParticleTypeRegistryObject<SimpleParticleType, SimpleParticleType> SMOKE = PARTICLE_TYPES.registerBasicParticle("smoke");
    public static final ParticleTypeRegistryObject<SimpleParticleType, SimpleParticleType> FLASH = PARTICLE_TYPES.registerBasicParticle("flash");
    public static final ParticleTypeRegistryObject<SimpleParticleType, SimpleParticleType> EXPLOSION = PARTICLE_TYPES.registerBasicParticle("explosion");
    public static final ParticleTypeRegistryObject<SimpleParticleType, SimpleParticleType> EXPLOSION_SEED = PARTICLE_TYPES.registerBasicParticle("explosion_seed");
    public static final ParticleTypeRegistryObject<SimpleParticleType, SimpleParticleType> VANILLA_FLASH = PARTICLE_TYPES.registerBasicParticle("vanilla_flash");

    public static void init() {
    }
}