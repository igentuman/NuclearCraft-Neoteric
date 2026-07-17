package igentuman.nc.pipe.cap;

import igentuman.nc.block.pipe.entity.PipeConnectorBE;
import igentuman.nc.pipe.ConnectorMode;
import igentuman.nc.pipe.PipeNetwork;
import igentuman.nc.pipe.PipeNetworkManager;
import net.minecraft.server.level.ServerLevel;

abstract class NetworkHandler {

    protected final PipeConnectorBE connector;

    protected NetworkHandler(PipeConnectorBE connector) {
        this.connector = connector;
    }

    protected ServerLevel level() {
        return connector.getLevel() instanceof ServerLevel sl ? sl : null;
    }

    protected PipeNetworkManager manager() {
        ServerLevel level = level();
        return level == null ? null : PipeNetworkManager.get(level.dimension());
    }

    protected PipeNetwork network() {
        return connector.getNetwork();
    }

    /** Packed position of the owning connector, skipped so content never routes back into its own block. */
    protected long self() {
        return connector.getBlockPos().asLong();
    }

    /**
     * True when this handler must move nothing right now: the connector left DEFAULT mode (a stale wrapper a
     * neighbour still holds), or its redstone gate is off this tick (§3, §5). Every op checks this first.
     */
    protected boolean blocked() {
        return connector.getMode() != ConnectorMode.DEFAULT || !connector.redstoneAllows();
    }
}
