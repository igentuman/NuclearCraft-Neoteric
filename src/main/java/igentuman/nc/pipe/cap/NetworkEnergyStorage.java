package igentuman.nc.pipe.cap;

import igentuman.nc.block_entity.pipe.PipeConnectorBE;
import igentuman.nc.pipe.PipeCapabilityType;
import igentuman.nc.pipe.PipeNetwork;
import igentuman.nc.pipe.PipeNetworkManager;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;

/** Routes native energy operations through the network in the caller's transaction. */
public class NetworkEnergyStorage extends NetworkHandler implements EnergyHandler {

    public NetworkEnergyStorage(PipeConnectorBE connector, Direction side) {
        super(connector, side);
    }

    @Override
    public int insert(int maxReceive, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(maxReceive);
        if (maxReceive == 0 || connector.isRemoved() || blocked() || !connector.isEnabled(PipeCapabilityType.ENERGY)) {
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
            PipeConnectorBE dest = manager.getConnectorBE(packed);
            if (dest == null || !dest.isEnabled(PipeCapabilityType.ENERGY)
                    || !dest.getMode().isDestination() || !dest.redstoneAllows()) {
                continue;
            }
            for (Direction face : Direction.values()) {
                if (isRequester(packed, face)) {
                    continue;
                }
                EnergyHandler h = dest.getExternalCapability(face, Capabilities.Energy.BLOCK);
                if (h == null) {
                    continue;
                }
                int accepted = h.insert(remaining, transaction);
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
    public int extract(int maxExtract, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(maxExtract);
        if (maxExtract == 0 || connector.isRemoved() || blocked() || !connector.isEnabled(PipeCapabilityType.ENERGY)) {
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
            PipeConnectorBE src = manager.getConnectorBE(packed);
            if (src == null || !src.isEnabled(PipeCapabilityType.ENERGY)
                    || !src.getMode().isSource() || !src.redstoneAllows()) {
                continue;
            }
            for (Direction face : Direction.values()) {
                if (isRequester(packed, face)) {
                    continue;
                }
                EnergyHandler h = src.getExternalCapability(face, Capabilities.Energy.BLOCK);
                if (h == null) {
                    continue;
                }
                int extracted = h.extract(remaining, transaction);
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
    public long getAmountAsLong() { return 0; }

    @Override
    public long getCapacityAsLong() { return Integer.MAX_VALUE; }
}
