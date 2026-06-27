package igentuman.nc.block_entity.catalyst;

/**
 * Catalyst categories a processor can support. Each supported type gets its own
 * catalyst slot on the processor; the enum ordinal determines that slot's order.
 */
public enum CatalystType {
    ENERGY,
    SPEED,
    EFFICIENCY
}
