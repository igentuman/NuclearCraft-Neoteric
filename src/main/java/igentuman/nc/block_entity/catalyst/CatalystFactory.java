package igentuman.nc.block_entity.catalyst;

import igentuman.nc.block_entity.GlobalBlockEntity;

@FunctionalInterface
public interface CatalystFactory {
    Catalyst create(GlobalBlockEntity host);
}
