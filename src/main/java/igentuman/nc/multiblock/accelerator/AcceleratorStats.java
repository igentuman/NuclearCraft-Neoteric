package igentuman.nc.multiblock.accelerator;

public record AcceleratorStats(
        int beamLength,
        long voltage,
        double dipoleField,
        double quadrupoleField,
        long energyPerTick,
        long heatPerTick,
        long heatCapacity,
        long coolingPerTick,
        long maximumTemperatureK,
        double efficiency
) {
    public static final AcceleratorStats EMPTY = new AcceleratorStats(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
}
