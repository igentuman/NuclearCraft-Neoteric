package igentuman.nc.multiblock.particle_chamber;

import igentuman.nc.api.multiblock.part.DetectorDef;
import igentuman.nc.api.multiblock.AbstractMultiblockCache;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class ParticleChamberCache extends AbstractMultiblockCache {

    private long workingEnergyPerTick;
    private double workingDetectorEfficiency;
    private int workingValidDetectors;
    private final List<BlockPos> workingBeamInputs = new ArrayList<>();
    private final List<BlockPos> workingBeamOutputs = new ArrayList<>();
    private final List<BlockPos> workingServicePorts = new ArrayList<>();

    public volatile ParticleChamberStats stats = ParticleChamberStats.EMPTY;
    public volatile List<BlockPos> beamInputs = List.of();
    public volatile List<BlockPos> beamOutputs = List.of();
    public volatile List<BlockPos> servicePorts = List.of();

    public void addDetector(DetectorDef detector) {
        workingValidDetectors++;
        workingDetectorEfficiency += detector.efficiency();
        workingEnergyPerTick = Math.addExact(workingEnergyPerTick, detector.energyPerTick());
    }

    public void addEnergyPerTick(long value) {
        workingEnergyPerTick = Math.addExact(workingEnergyPerTick, value);
    }

    public int workingValidDetectors() {
        return workingValidDetectors;
    }

    public void addBeamInput(BlockPos pos) {
        workingBeamInputs.add(pos.immutable());
    }

    public void addBeamOutput(BlockPos pos) {
        workingBeamOutputs.add(pos.immutable());
    }

    public void addServicePort(BlockPos pos) {
        workingServicePorts.add(pos.immutable());
    }

    @Override
    protected void resetWorkingData() {
        workingEnergyPerTick = 0;
        workingDetectorEfficiency = 0;
        workingValidDetectors = 0;
        workingBeamInputs.clear();
        workingBeamOutputs.clear();
        workingServicePorts.clear();
    }

    @Override
    protected void publishData() {
        stats = new ParticleChamberStats(workingEnergyPerTick, 1D + workingDetectorEfficiency, workingValidDetectors);
        beamInputs = List.copyOf(workingBeamInputs);
        beamOutputs = List.copyOf(workingBeamOutputs);
        servicePorts = List.copyOf(workingServicePorts);
    }
}
