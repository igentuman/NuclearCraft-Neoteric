package igentuman.nc.pipe;

public enum ConnectorMode {
    DISABLED,
    PULL,
    PUSH,
    DEFAULT;

    public ConnectorMode next() {
        ConnectorMode[] all = values();
        return all[(ordinal() + 1) % all.length];
    }

    public boolean isDestination() {
        return this == PUSH || this == DEFAULT;
    }

    public boolean isSource() {
        return this == PULL || this == DEFAULT;
    }
}
