package igentuman.nc.block_entity;

/** Implemented by multiblock controllers that expose comparator output and redstone control modes through their ports. */
public interface RedstoneModeController {

    /** Comparator strength (0-15) for an output mode (ENERGY/HEAT/PROGRESS/ITEMS). */
    int comparatorSignal(int mode);

    /** Applies an incoming redstone signal (0-15) for a control mode (SWITCH/MODERATOR). */
    void applyRedstoneInput(int mode, int signal);
}
