package igentuman.nc.content.particles;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * Particle stack handler utilities.
 * The old Capability<IParticleStackHandler> was removed during NeoForge migration.
 * Block entities use ParticleStorage directly instead of capability lookup.
 */
public class CapabilityParticleStackHandler {

    /**
     * Storage implementation for serializing/deserializing particle data
     */
    public static class ParticleHandlerStorage implements INBTSerializable<CompoundTag> {
        private final IParticleStackHandler handler;

        public ParticleHandlerStorage(IParticleStackHandler handler) {
            this.handler = handler;
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider provider) {
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
        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
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
