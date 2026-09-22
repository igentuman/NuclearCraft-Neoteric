package igentuman.nc.api.particle;

public interface IParticleHandler {

    int channels();

    ParticleStack snapshot(int channel);

    long capacity(int channel);

    ParticleStack insert(int channel, ParticleStack stack, ParticleAction action);

    ParticleStack extract(int channel, long maximumAmount, ParticleAction action);
}
