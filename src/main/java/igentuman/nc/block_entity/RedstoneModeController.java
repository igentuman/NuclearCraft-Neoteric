package igentuman.nc.block_entity;

public interface RedstoneModeController {

    int comparatorSignal(int mode);

    void applyRedstoneInput(int mode, int signal);
}
