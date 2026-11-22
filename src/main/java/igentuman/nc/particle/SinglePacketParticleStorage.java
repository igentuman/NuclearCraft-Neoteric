package igentuman.nc.particle;

import igentuman.nc.api.particle.ParticleStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Accepts a particle packet only while its single channel is empty. */
public final class SinglePacketParticleStorage extends ParticleStorage {

    public SinglePacketParticleStorage(long capacity) {
        super(1, capacity);
    }

    @Override
    public ParticleStack insert(int channel, ParticleStack stack, TransactionContext transaction) {
        if (!snapshot(channel).isEmpty()) return stack;
        return super.insert(channel, stack, transaction);
    }
}
