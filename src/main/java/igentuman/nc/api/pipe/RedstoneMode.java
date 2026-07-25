package igentuman.nc.api.pipe;

public enum RedstoneMode {
    ALWAYS,
    ON_SIGNAL;

    public RedstoneMode next() {
        RedstoneMode[] all = values();
        return all[(ordinal() + 1) % all.length];
    }
}
