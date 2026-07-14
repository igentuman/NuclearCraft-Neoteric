package igentuman.nc.block_entity.catalyst;

import igentuman.nc.block_entity.GlobalBlockEntity;

/** Factory that constructs a {@link Catalyst} behavior instance bound to a given host block entity. */
@FunctionalInterface
public interface CatalystFactory {
    Catalyst create(GlobalBlockEntity host);
}
