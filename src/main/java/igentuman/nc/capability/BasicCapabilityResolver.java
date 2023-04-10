package igentuman.nc.capability;

import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@NothingNullByDefault
public class BasicCapabilityResolver implements ICapabilityResolver {

    public static <T> BasicCapabilityResolver create(Capability<T> supportedCapability, NonNullSupplier<T> supplier) {
        return new BasicCapabilityResolver(supplier, supportedCapability);
    }

    /**
     * Creates a capability resolver that strongly caches the result of the supplier. Persisting the calculated value through capability invalidation.
     */
    public static <T> BasicCapabilityResolver persistent(Capability<T> supportedCapability, NonNullSupplier<T> supplier) {
        return create(supportedCapability, supplier instanceof NonNullLazy ? supplier : NonNullLazy.of(supplier));
    }

    /**
     * Creates a capability resolver of a constant value. Usually {@code this} for tiles.
     */
    public static <T> BasicCapabilityResolver constant(Capability<T> supportedCapability, T value) {
        return create(supportedCapability, () -> value);
    }

    private final List<Capability<?>> supportedCapability;
    private final NonNullSupplier<?> supplier;
    @Nullable
    private LazyOptional<?> cachedCapability;

    @SafeVarargs
    protected <T> BasicCapabilityResolver(NonNullSupplier<T> supplier, Capability<? super T>... supportedCapabilities) {
        this.supportedCapability = List.of(supportedCapabilities);
        this.supplier = supplier;
    }

    @Override
    public List<Capability<?>> getSupportedCapabilities() {
        return supportedCapability;
    }

    @Override
    public <T> LazyOptional<T> resolve(Capability<T> capability, @Nullable Direction side) {
        if (cachedCapability == null || !cachedCapability.isPresent()) {
            //If the capability has not been retrieved yet, or it is not valid then recreate it
            cachedCapability = LazyOptional.of(supplier);
        }
        return cachedCapability.cast();
    }

    @Override
    public void invalidate(Capability<?> capability, @Nullable Direction side) {
        //We only have one capability so just invalidate everything
        invalidateAll();
    }

    @Override
    public void invalidateAll() {
        if (cachedCapability != null && cachedCapability.isPresent()) {
            cachedCapability.invalidate();
            cachedCapability = null;
        }
    }
}