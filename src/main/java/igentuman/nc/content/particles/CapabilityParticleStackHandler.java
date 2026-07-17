package igentuman.nc.content.particles;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class CapabilityParticleStackHandler
{
    public static final Capability<IParticleStackHandler> PARTICLE_HANDLER_CAPABILITY =
        CapabilityManager.get(new CapabilityToken<>(){});

    public static void register(RegisterCapabilitiesEvent event)
    {
        event.register(IParticleStackHandler.class);
    }
    
    /**
     * Creates a LazyOptional for a particle storage
     */
    public static LazyOptional<IParticleStackHandler> createHandler(ParticleStorage storage) {
        return LazyOptional.of(() -> storage);
    }
    
    /**
     * Storage implementation for serializing/deserializing capability data
     */
    public static class ParticleHandlerStorage implements INBTSerializable<CompoundTag> {
        private final IParticleStackHandler handler;
        
        public ParticleHandlerStorage(IParticleStackHandler handler) {
            this.handler = handler;
        }
        
        @Override
        public CompoundTag serializeNBT() {
            if (!(handler instanceof IParticleStorage))
                throw new RuntimeException("IParticleStackHandler instance does not implement IParticleStorage");
            
            CompoundTag nbt = new CompoundTag();
            IParticleStorage tank = (IParticleStorage) handler;
            ParticleStack particle = tank.getParticleStack();
            
            if (particle != null) {
                particle.writeToNBT(nbt);
            } else {
                nbt.putString("Empty", "");
            }
            
            nbt.putLong("MaxEnergy", tank.getMaxEnergy());
            nbt.putInt("Capacity", tank.getCapacity());
            nbt.putLong("MinEnergy", tank.getMinEnergy());
            
            return nbt;
        }
        
        @Override
        public void deserializeNBT(CompoundTag nbt) {
            if (!(handler instanceof ParticleStorage))
                throw new RuntimeException("IParticleStackHandler instance is not instance of ParticleStorage");
            
            ParticleStorage tank = (ParticleStorage) handler;
            tank.setMaxEnergy(nbt.getLong("MaxEnergy"));
            tank.setCapacity(nbt.getInt("Capacity"));
            tank.setMinEnergy(nbt.getLong("MinEnergy"));
            tank.readFromNBT(nbt);
        }
    }
}
