package igentuman.nc.multiblock.accelerator;

public class RingAcceleratorCache extends AcceleratorCache {

    private int workingOuterSide;

    public volatile int outerSide;

    public void setWorkingOuterSide(int value) {
        workingOuterSide = value;
    }

    public double radius() {
        return Math.max(1D, (outerSide - 4) / 2D);
    }

    @Override
    protected void resetAcceleratorData() {
        workingOuterSide = 0;
    }

    @Override
    protected void publishAcceleratorData() {
        outerSide = workingOuterSide;
    }
}
