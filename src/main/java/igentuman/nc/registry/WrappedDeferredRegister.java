package igentuman.nc.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Function;
import java.util.function.Supplier;

public class WrappedDeferredRegister<T> {

    protected final DeferredRegister<T> internal;

    protected WrappedDeferredRegister(DeferredRegister<T> internal) {
        this.internal = internal;
    }

    protected WrappedDeferredRegister(String modid, ResourceKey<? extends Registry<T>> registryName) {
        this(DeferredRegister.create(registryName, modid));
    }

    @SuppressWarnings("unchecked")
    protected <I extends T, W extends WrappedRegistryObject<I>> W register(String name, Supplier<? extends I> sup, Function<DeferredHolder<T, I>, W> objectWrapper) {
        DeferredHolder<T, I> holder = internal.register(name, sup);
        return objectWrapper.apply(holder);
    }

    public void register(IEventBus bus) {
        internal.register(bus);
    }
}
