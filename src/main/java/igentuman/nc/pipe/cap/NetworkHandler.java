package igentuman.nc.pipe.cap;

import igentuman.nc.block_entity.pipe.PipeConnectorBE;
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

    protected long self() {
        return connector.getBlockPos().asLong();
    }

    protected boolean blocked() {
        return connector.getMode() != ConnectorMode.DEFAULT || !connector.redstoneAllows();
    }
}
