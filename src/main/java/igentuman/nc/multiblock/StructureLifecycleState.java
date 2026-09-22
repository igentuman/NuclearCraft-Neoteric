package igentuman.nc.multiblock;

public enum StructureLifecycleState {
    UNFORMED,
    DISCOVERING,
    VALIDATING,
    WAITING_FOR_CHUNKS,
    APPLYING,
    FORMED,
    DIRTY,
    BREAKING,
    SUSPENDED,
    DISPOSED
}
