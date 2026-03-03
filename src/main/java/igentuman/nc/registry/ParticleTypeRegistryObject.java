package igentuman.nc.registry;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ParticleTypeRegistryObject<PARTICLE extends ParticleOptions, TYPE extends ParticleType<PARTICLE>> extends WrappedRegistryObject<TYPE> {

    public ParticleTypeRegistryObject(DeferredHolder<?, ?> registryObject) {
        super(registryObject);
    }
}
