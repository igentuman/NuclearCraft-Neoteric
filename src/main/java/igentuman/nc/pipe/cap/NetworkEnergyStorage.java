package igentuman.nc.pipe.cap;

import igentuman.nc.block_entity.pipe.PipeConnectorBE;
import igentuman.nc.pipe.PipeCapabilityType;
import igentuman.nc.pipe.PipeNetwork;
import igentuman.nc.pipe.PipeNetworkManager;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class NetworkEnergyStorage extends NetworkHandler implements IEnergyStorage {

    public NetworkEnergyStorage(PipeConnectorBE connector) {
        super(connector);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (maxReceive <= 0 || blocked()) {
            return 0;
        }
        ServerLevel level = level();
        PipeNetwork net = network();
        PipeNetworkManager manager = manager();
        if (level == null || net == null || manager == null) {
            return 0;
        }
        int remaining = maxReceive;
        int total = 0;
        for (long packed : net.getDestinations(PipeCapabilityType.ENERGY, manager)) {
            if (packed == self()) {
                continue;
            }
            PipeConnectorBE dest = manager.getConnectorBE(packed);
            if (dest == null) {
                continue;
            }
            for (Direction face : Direction.values()) {
                IEnergyStorage h = dest.getExternalCapability(face, Capabilities.EnergyStorage.BLOCK);
                if (h == null || !h.canReceive()) {
                    continue;
                }
                int accepted = h.receiveEnergy(remaining, simulate);
                if (accepted > 0) {
                    total += accepted;
                    remaining -= accepted;
                    if (remaining <= 0) {
                        return total;
                    }
                }
            }
        }
        return total;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (maxExtract <= 0 || blocked()) {
            return 0;
        }
        ServerLevel level = level();
        PipeNetwork net = network();
        PipeNetworkManager manager = manager();
        if (level == null || net == null || manager == null) {
            return 0;
        }
        int remaining = maxExtract;
        int total = 0;
        for (long packed : net.getSources(PipeCapabilityType.ENERGY, manager)) {
            if (packed == self()) {
                continue;
            }
            PipeConnectorBE src = manager.getConnectorBE(packed);
            if (src == null) {
                continue;
            }
            for (Direction face : Direction.values()) {
                IEnergyStorage h = src.getExternalCapability(face, Capabilities.EnergyStorage.BLOCK);
                if (h == null || !h.canExtract()) {
                    continue;
                }
                int extracted = h.extractEnergy(remaining, simulate);
                if (extracted > 0) {
                    total += extracted;
                    remaining -= extracted;
                    if (remaining <= 0) {
                        return total;
                    }
                }
            }
        }
        return total;
    }

    @Override
    public int getEnergyStored() {
        return 0;
    }

    @Override
    public int getMaxEnergyStored() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }
}
