package igentuman.nc.registry;

import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

@NothingNullByDefault
public class WrappedRegistryObject<T> implements Supplier<T>, INamedEntry {

    protected final Supplier<T> registryObject;
    private final ResourceLocation id;

    @SuppressWarnings("unchecked")
    protected WrappedRegistryObject(DeferredHolder<?, ?> registryObject) {
        this.registryObject = () -> (T) registryObject.get();
        this.id = registryObject.getId();
    }

    @Override
    public T get() {
        return registryObject.get();
    }

    @Override
    public String getInternalRegistryName() {
        return id.getPath();
    }
}
