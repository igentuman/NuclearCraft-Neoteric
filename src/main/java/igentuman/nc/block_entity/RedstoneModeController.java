package igentuman.nc.block_entity;

/**
 * Implemented by multiblock controllers that expose redstone signal modes through their ports.
 * The port reads {@link #comparatorSignal} for output modes and pushes incoming redstone via
 * {@link #applyRedstoneInput} for control modes. Mode constants live on
 * {@link igentuman.nc.block_entity.fission.FissionPortBE}.
 */
public interface RedstoneModeController {

    /** Comparator strength (0-15) for an output mode (ENERGY/HEAT/PROGRESS/ITEMS). */
    int comparatorSignal(int mode);

    /** Applies an incoming redstone signal (0-15) for a control mode (SWITCH/MODERATOR). */
    void applyRedstoneInput(int mode, int signal);
}
