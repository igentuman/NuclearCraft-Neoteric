package igentuman.api.nc;

public interface SideModeToggleable {

    enum SideMode {
        DEFAULT, IN, OUT, DISABLED
    }

    SideMode toggleSideConfig(int direction);
}
