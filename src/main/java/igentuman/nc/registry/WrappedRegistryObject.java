package igentuman.nc.registry;

import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

@NothingNullByDefault
public class WrappedRegistryObject<T> implements Supplier<T>, INamedEntry {

    protected RegistryObject<T> registryObject;

    protected WrappedRegistryObject(RegistryObject<T> registryObject) {
        this.registryObject = registryObject;
    }

    @Override
    public T get() {
        return registryObject.get();
    }

    @Override
    public String getInternalRegistryName() {
        return registryObject.getId().getPath();
    }
}