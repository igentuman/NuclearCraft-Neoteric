package igentuman.nc.multiblock.particle_chamber;

public record ParticleChamberStats(long energyPerTick, double efficiency, int validDetectorCount) {

    public static final ParticleChamberStats EMPTY = new ParticleChamberStats(0, 1D, 0);
}
