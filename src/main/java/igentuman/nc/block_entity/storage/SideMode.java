package igentuman.nc.block_entity.storage;

public enum SideMode {
    DEFAULT, IN, OUT, DISABLED;

    public SideMode next() {
        return values()[(ordinal() + 1) % values().length];
    }
}
