package igentuman.nc.content.particles;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Capability provider for particle storage
 * Use this class to attach particle storage capabilities to blocks, items, etc.
 */
public class ParticleCapabilityProvider implements ICapabilitySerializable<CompoundTag> {
    
    private final ParticleStorage storage;
    private final LazyOptional<IParticleStackHandler> lazyHandler;
    private final CapabilityParticleStackHandler.ParticleHandlerStorage serializer;
    
    /**
     * Create a new particle capability provider
     * 
     * @param initialStack Initial particle stack (can be null)
     * @param maxEnergy Maximum energy of particles
     * @param capacity Maximum number of particles
     * @param minEnergy Minimum energy of particles
     */
    public ParticleCapabilityProvider(@Nullable ParticleStack initialStack, long maxEnergy, int capacity, long minEnergy) {
        this.storage = new ParticleStorage(initialStack, maxEnergy, capacity, minEnergy);
        this.lazyHandler = LazyOptional.of(() -> storage);
        this.serializer = new CapabilityParticleStackHandler.ParticleHandlerStorage(storage);
    }
    
    /**
     * Create a new particle capability provider with default min energy of 0
     * 
     * @param initialStack Initial particle stack (can be null)
     * @param maxEnergy Maximum energy of particles
     * @param capacity Maximum number of particles
     */
    public ParticleCapabilityProvider(@Nullable ParticleStack initialStack, long maxEnergy, int capacity) {
        this(initialStack, maxEnergy, capacity, 0);
    }
    
    /**
     * Create a new particle capability provider with default capacity of Integer.MAX_VALUE
     * 
     * @param initialStack Initial particle stack (can be null)
     * @param maxEnergy Maximum energy of particles
     */
    public ParticleCapabilityProvider(@Nullable ParticleStack initialStack, long maxEnergy) {
        this(initialStack, maxEnergy, Integer.MAX_VALUE, 0);
    }
    
    /**
     * Create a new particle capability provider with default values
     */
    public ParticleCapabilityProvider() {
        this(null, Long.MAX_VALUE, Integer.MAX_VALUE, 0);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityParticleStackHandler.PARTICLE_HANDLER_CAPABILITY) {
            return lazyHandler.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return serializer.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        serializer.deserializeNBT(nbt);
    }
    
    /**
     * Invalidate the capability when it's no longer needed
     * Call this method when the block is removed or the item is destroyed
     */
    public void invalidate() {
        lazyHandler.invalidate();
    }
}