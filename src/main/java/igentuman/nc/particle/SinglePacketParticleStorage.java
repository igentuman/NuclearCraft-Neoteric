package igentuman.nc.particle;

import igentuman.nc.api.particle.ParticleAction;
import igentuman.nc.api.particle.ParticleStack;

public final class SinglePacketParticleStorage extends ParticleStorage {

    public SinglePacketParticleStorage(long capacity) {
        super(1, capacity);
    }

    @Override
    public ParticleStack insert(int channel, ParticleStack stack, ParticleAction action) {
        if (!snapshot(channel).isEmpty()) return stack;
        return super.insert(channel, stack, action);
    }
}
