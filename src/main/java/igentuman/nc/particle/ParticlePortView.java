package igentuman.nc.particle;

import igentuman.nc.api.particle.IParticleHandler;
import igentuman.nc.api.particle.ParticleAction;
import igentuman.nc.api.particle.ParticleStack;

public final class ParticlePortView implements IParticleHandler {

    private final IParticleHandler controllerStorage;
    private final int channel;
    private final Access access;

    public ParticlePortView(IParticleHandler controllerStorage, int channel, Access access) {
        this.controllerStorage = controllerStorage;
        this.channel = channel;
        this.access = access;
    }

    public enum Access {
        INPUT,
        OUTPUT,
        DISABLED
    }

    @Override
    public int channels() {
        return 1;
    }

    @Override
    public ParticleStack snapshot(int channel) {
        validateExposedChannel(channel);
        return controllerStorage.snapshot(this.channel);
    }

    @Override
    public long capacity(int channel) {
        validateExposedChannel(channel);
        return controllerStorage.capacity(this.channel);
    }

    @Override
    public ParticleStack insert(int channel, ParticleStack stack, ParticleAction action) {
        validateExposedChannel(channel);
        if (access != Access.INPUT) {
            return stack;
        }
        return controllerStorage.insert(this.channel, stack, action);
    }

    @Override
    public ParticleStack extract(int channel, long maximumAmount, ParticleAction action) {
        validateExposedChannel(channel);
        if (access != Access.OUTPUT) {
            return ParticleStack.EMPTY;
        }
        return controllerStorage.extract(this.channel, maximumAmount, action);
    }

    public Access access() {
        return access;
    }

    private static void validateExposedChannel(int channel) {
        if (channel != 0) throw new IndexOutOfBoundsException("Particle port exposes only channel 0");
    }
}
