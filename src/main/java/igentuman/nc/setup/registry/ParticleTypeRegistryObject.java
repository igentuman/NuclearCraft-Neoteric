package igentuman.nc.setup.registry;

import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;

public class ParticleTypeRegistryObject<PARTICLE extends IParticleData, TYPE extends ParticleType<PARTICLE>> extends WrappedRegistryObject<TYPE> {

    public ParticleTypeRegistryObject(RegistryObject<TYPE> registryObject) {
        super(registryObject);
    }

    @Nonnull
    public TYPE getParticleType() {
        return get();
    }
}