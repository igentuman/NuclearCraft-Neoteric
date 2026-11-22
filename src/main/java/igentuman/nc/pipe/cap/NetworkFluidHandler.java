package igentuman.nc.pipe.cap;

import igentuman.nc.api.pipe.ConnectorMode;
import igentuman.nc.pipe.PipeCapabilityType;
import igentuman.nc.pipe.PipeNetworkManager;
import igentuman.nc.block_entity.pipe.PipeConnectorBE;
import igentuman.nc.util.caps.RoutingFluidHandler;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.Capabilities;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/** Resolves current fluid endpoints and routes operations in the caller's transaction. */
public class NetworkFluidHandler extends RoutingFluidHandler {
    public NetworkFluidHandler(PipeConnectorBE connector, Direction side) {
        super(() -> endpoints(connector, side));
    }

    private static List<Endpoint> endpoints(PipeConnectorBE connector, Direction side) {
        if (connector.isRemoved() || connector.getMode() != ConnectorMode.DEFAULT
                || !connector.redstoneAllows() || !connector.isEnabled(PipeCapabilityType.FLUID)
                || !(connector.getLevel() instanceof ServerLevel level)) return List.of();
        var network = connector.getNetwork();
        if (network == null) return List.of();
        var manager = PipeNetworkManager.get(level.dimension());
        var connectors = new LinkedHashSet<>(network.getDestinations(PipeCapabilityType.FLUID, manager));
        connectors.addAll(network.getSources(PipeCapabilityType.FLUID, manager));
        var endpoints = new ArrayList<Endpoint>();
        for (long packed : connectors) {
            var remote = manager.getConnectorBE(packed);
            if (remote == null || remote.isRemoved() || !remote.isEnabled(PipeCapabilityType.FLUID)
                    || !remote.redstoneAllows()) continue;
            boolean insert = remote.getMode().isDestination();
            boolean extract = remote.getMode().isSource();
            if (!insert && !extract) continue;
            for (Direction face : Direction.values()) {
                if (packed == connector.getBlockPos().asLong() && (side == null || face == side)) continue;
                var handler = remote.getExternalCapability(face, Capabilities.Fluid.BLOCK);
                if (handler != null) endpoints.add(new Endpoint(handler, insert, extract));
            }
        }
        return endpoints;
    }
}
