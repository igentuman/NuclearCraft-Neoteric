package igentuman.nc.multiblock.accelerator;

import net.minecraft.core.Direction;

public class LinearAcceleratorCache extends AcceleratorCache {

    private Direction workingForward;

    public volatile Direction forward = Direction.NORTH;

    public void setWorkingForward(Direction direction) {
        workingForward = direction;
    }

    @Override
    protected void resetAcceleratorData() {
        workingForward = null;
    }

    @Override
    protected void publishAcceleratorData() {
        if (workingForward != null) forward = workingForward;
    }
}
