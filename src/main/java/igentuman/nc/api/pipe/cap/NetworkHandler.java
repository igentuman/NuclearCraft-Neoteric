package igentuman.nc.api.pipe.cap;

import igentuman.nc.block_entity.pipe.PipeConnectorBE;
import igentuman.nc.api.pipe.ConnectorMode;
import igentuman.nc.api.pipe.PipeNetwork;
import igentuman.nc.api.pipe.PipeNetworkManager;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

abstract class NetworkHandler {

    protected final PipeConnectorBE connector;
    protected final Direction side;

    protected NetworkHandler(PipeConnectorBE connector, Direction side) {
        this.connector = connector;
        this.side = side;
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

    protected boolean isRequester(long packed, Direction face) {
        return packed == self() && (side == null || face == side);
    }

    protected boolean blocked() {
        return connector.getMode() != ConnectorMode.DEFAULT || !connector.redstoneAllows();
    }
}
