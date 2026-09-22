package igentuman.nc.multiblock.validation;

public enum ValidationStatus {
    IN_PROGRESS,
    WAITING_FOR_CHUNK,
    VALID,
    INVALID,
    STALE,
    CANCELLED
}
