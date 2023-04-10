package igentuman.nc.capability;

import igentuman.nc.NuclearCraft;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BooleanSupplier;

@NothingNullByDefault
public class CapabilityCache {

    private final Map<Capability<?>, ICapabilityResolver> capabilityResolvers = new HashMap<>();
    /**
     * List of unique resolvers to make invalidating all easier as some resolvers (energy) may support multiple capabilities.
     */
    private final List<ICapabilityResolver> uniqueResolvers = new ArrayList<>();
    private final Set<Capability<?>> alwaysDisabled = new HashSet<>();
    private final Map<Capability<?>, List<BooleanSupplier>> semiDisabled = new HashMap<>();

    /**
     * Adds a capability resolver to the list of resolvers for this cache.
     */
    public void addCapabilityResolver(ICapabilityResolver resolver) {
        uniqueResolvers.add(resolver);
        List<Capability<?>> supportedCapabilities = resolver.getSupportedCapabilities();
        for (Capability<?> supportedCapability : supportedCapabilities) {
            //Note: We add the capability regardless of if it is registered as we will just short circuit and always disable the capability
            // if it isn't in use by the time the capability is queried. In theory we shouldn't ever be getting created before the capabilities
            // have been registered, but just in case we ensure it works properly
            if (capabilityResolvers.put(supportedCapability, resolver) != null) {
                NuclearCraft.LOGGER.warn("Multiple capability resolvers registered for {}. Overriding", supportedCapability.getName(), new Exception());
            }
        }
    }

    /**
     * Marks all the given capabilities as always being disabled.
     */
    public void addDisabledCapabilities(Capability<?>... capabilities) {
        Collections.addAll(alwaysDisabled, capabilities);
    }

    /**
     * Marks all the given capabilities as always being disabled.
     */
    public void addDisabledCapabilities(Collection<Capability<?>> capabilities) {
        alwaysDisabled.addAll(capabilities);
    }




    /**
     * Checks if the given capability can be resolved by this capability cache.
     */
    public boolean canResolve(Capability<?> capability) {
        return capabilityResolvers.containsKey(capability);
    }

    /**
     * Gets a capability on the given side, ensuring that it can be resolved and that it is not disabled.
     */
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        return getCapabilityUnchecked(capability, side);
    }

    /**
     * Gets a capability on the given side not checking to ensure that it is not disabled.
     */
    public <T> LazyOptional<T> getCapabilityUnchecked(Capability<T> capability, @Nullable Direction side) {
        ICapabilityResolver capabilityResolver = capabilityResolvers.get(capability);
        if (capabilityResolver == null) {
            return LazyOptional.empty();
        }
        return capabilityResolver.resolve(capability, side);
    }

    /**
     * Invalidates the given capability on the given side.
     *
     * @param capability Capability
     * @param side       Side
     */
    public void invalidate(Capability<?> capability, @Nullable Direction side) {
        ICapabilityResolver capabilityResolver = capabilityResolvers.get(capability);
        if (capabilityResolver != null) {
            capabilityResolver.invalidate(capability, side);
        }
    }

    /**
     * Invalidates all cached capabilities.
     */
    public void invalidateAll() {
        uniqueResolvers.forEach(ICapabilityResolver::invalidateAll);
    }
}