package igentuman.nc.handler.sided;

import igentuman.nc.handler.sided.capability.FluidCapabilityHandler;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.handler.sided.capability.MSRFluidCapabilityHandler;
import igentuman.nc.handler.sided.capability.MSRVolumeTank;
import igentuman.nc.handler.sided.capability.NcFluidTank;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

/**
 * Content handler for the MSR controller. Routes fluid and item inputs through the reactor's
 * shared internal volume: salt and coolant input tanks plus the pebble input slot all draw from
 * one budget, so once the chamber is full no more can be inserted until something is extracted.
 */
public class MSRContentHandler extends SidedContentHandler {

    public MSRContentHandler(int inputItemSlots, int outputItemSlots, int inputFluidSlots, int outputFluidSlots, int... tankCapacities) {
        super(inputItemSlots, outputItemSlots, inputFluidSlots, outputFluidSlots, tankCapacities);
    }

    @Override
    protected FluidCapabilityHandler createFluidHandler(int inputFluidSlots, int outputFluidSlots, int inputTankSize, int outputTankSize) {
        return new MSRFluidCapabilityHandler(inputFluidSlots, outputFluidSlots, inputTankSize, outputTankSize);
    }

    /**
     * Wires the reactor's free-volume query into the cold-salt input tank (0) and the pebble input
     * slot. The hot-salt output tank (1) is NOT gated — it is filled internally by the cold→hot
     * conversion (volume-conserving) and drained out by pipes. Called once by the controller after
     * construction.
     */
    public void setVolumeGate(DoubleSupplier freeVolume, double volumePerPebble) {
        if (fluidHandler != null && !fluidHandler.tanks.isEmpty()
                && fluidHandler.tanks.get(0) instanceof MSRVolumeTank tank) {
            tank.setFreeVolume(freeVolume);
        }
    }

    /**
     * Wires the player-set molten-salt rates (mB/tick): input rate caps cold-salt fill (tank 0),
     * output rate caps hot-salt drain (tank 1).
     */
    public void setRateGate(IntSupplier inputRateMb, IntSupplier outputRateMb) {
        if (fluidHandler == null || fluidHandler.tanks.size() < 2) return;
        if (fluidHandler.tanks.get(0) instanceof MSRVolumeTank cold) {
            cold.setMaxFillPerTick(inputRateMb);
        }
        if (fluidHandler.tanks.get(1) instanceof MSRVolumeTank hot) {
            hot.setMaxDrainPerTick(outputRateMb);
        }
    }

    /** Reset per-tick throughput counters; call once at the start of each reactor tick. */
    public void resetRateCounters() {
        if (fluidHandler == null) return;
        for (NcFluidTank tank : fluidHandler.tanks) {
            if (tank instanceof MSRVolumeTank mt) {
                mt.resetTickCounters();
            }
        }
    }
}
