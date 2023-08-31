package igentuman.nc.block;

public interface ISizeToggable {
    public static enum SideMode {
        DEFAULT, IN, OUT, DISABLED
    }

    SideMode toggleSideConfig(int direction);
}
