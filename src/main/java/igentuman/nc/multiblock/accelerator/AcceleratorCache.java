package igentuman.nc.multiblock.accelerator;

import igentuman.nc.api.multiblock.AbstractMultiblockCache;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public abstract class AcceleratorCache extends AbstractMultiblockCache {

    private int workingBeamLength;
    private long workingVoltage;
    private double workingDipoleField;
    private double workingQuadrupoleField;
    private long workingEnergyPerTick;
    private long workingHeatPerTick;
    private long workingHeatCapacity;
    private long workingCoolingPerTick;
    private long workingMaximumTemperatureK;
    private double workingComponentEfficiency;
    private int workingComponentCount;
    private final List<BlockPos> workingBeamInputs = new ArrayList<>();
    private final List<BlockPos> workingBeamOutputs = new ArrayList<>();
    private final List<BlockPos> workingIonSources = new ArrayList<>();
    private final List<BlockPos> workingServicePorts = new ArrayList<>();

    public volatile AcceleratorStats stats = AcceleratorStats.EMPTY;
    public volatile List<BlockPos> beamInputs = List.of();
    public volatile List<BlockPos> beamOutputs = List.of();
    public volatile List<BlockPos> ionSources = List.of();
    public volatile List<BlockPos> servicePorts = List.of();

    public void setWorkingBeamLength(int value) {
        workingBeamLength = Math.max(0, value);
    }

    public void addVoltage(long value) {
        workingVoltage = Math.addExact(workingVoltage, value);
    }

    public void addDipoleField(double value) {
        workingDipoleField += value;
    }

    public void addQuadrupoleField(double value) {
        workingQuadrupoleField += value;
    }

    public void addEnergyPerTick(long value) {
        workingEnergyPerTick = Math.addExact(workingEnergyPerTick, value);
    }

    public void addHeatPerTick(long value) {
        workingHeatPerTick = Math.addExact(workingHeatPerTick, value);
    }

    public void setHeatCapacity(long value) {
        workingHeatCapacity = value;
    }

    public void addCoolingPerTick(long value) {
        workingCoolingPerTick = Math.addExact(workingCoolingPerTick, value);
    }

    public void includeMaximumTemperatureK(long value) {
        if (value <= 0) return;
        workingMaximumTemperatureK = workingMaximumTemperatureK == 0
                ? value : Math.min(workingMaximumTemperatureK, value);
    }

    public void addComponentEfficiency(double value) {
        workingComponentEfficiency += value;
        workingComponentCount++;
    }

    public void addBeamInput(BlockPos pos) {
        workingBeamInputs.add(pos.immutable());
    }

    public void addBeamOutput(BlockPos pos) {
        workingBeamOutputs.add(pos.immutable());
    }

    public void addIonSource(BlockPos pos) {
        workingIonSources.add(pos.immutable());
    }

    public void addServicePort(BlockPos pos) {
        workingServicePorts.add(pos.immutable());
    }

    @Override
    protected final void resetWorkingData() {
        workingBeamLength = 0;
        workingVoltage = 0;
        workingDipoleField = 0;
        workingQuadrupoleField = 0;
        workingEnergyPerTick = 0;
        workingHeatPerTick = 0;
        workingHeatCapacity = 0;
        workingCoolingPerTick = 0;
        workingMaximumTemperatureK = 0;
        workingComponentEfficiency = 0;
        workingComponentCount = 0;
        workingBeamInputs.clear();
        workingBeamOutputs.clear();
        workingIonSources.clear();
        workingServicePorts.clear();
        resetAcceleratorData();
    }

    @Override
    protected final void publishData() {
        stats = new AcceleratorStats(workingBeamLength, workingVoltage, workingDipoleField, workingQuadrupoleField,
                workingEnergyPerTick, workingHeatPerTick, workingHeatCapacity, workingCoolingPerTick,
                workingMaximumTemperatureK,
                workingComponentCount == 0 ? 0 : workingComponentEfficiency / workingComponentCount);
        beamInputs = List.copyOf(workingBeamInputs);
        beamOutputs = List.copyOf(workingBeamOutputs);
        ionSources = List.copyOf(workingIonSources);
        servicePorts = List.copyOf(workingServicePorts);
        publishAcceleratorData();
    }

    protected void resetAcceleratorData() {
    }

    protected void publishAcceleratorData() {
    }
}
