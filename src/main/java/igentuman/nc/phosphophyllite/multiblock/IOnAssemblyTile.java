package igentuman.nc.phosphophyllite.multiblock;

public interface IOnAssemblyTile {
    /**
     * Called when a multiblock is first assembled or resumed from a paused state
     * called after a controller's onAssembled and blockstates are updated
     */
    void onAssembly();
}
