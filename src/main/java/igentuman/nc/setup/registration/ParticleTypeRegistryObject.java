package igentuman.nc.setup.registration;

import igentuman.nc.setup.recipes.WrappedRegistryObject;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.registries.RegistryObject;

public class ParticleTypeRegistryObject<PARTICLE extends ParticleOptions, TYPE extends ParticleType<PARTICLE>> extends WrappedRegistryObject<TYPE> {

    public ParticleTypeRegistryObject(RegistryObject<TYPE> registryObject) {
        super(registryObject);
    }
}