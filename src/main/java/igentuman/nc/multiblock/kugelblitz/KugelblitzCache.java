package igentuman.nc.multiblock.kugelblitz;

import igentuman.nc.api.multiblock.AbstractMultiblockCache;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public class KugelblitzCache extends AbstractMultiblockCache {

    private int workingTransformers;
    private int workingFluxRegulators;
    private int workingStabilizers;
    private BlockPos workingCenter;

    public volatile int transformers;
    public volatile int fluxRegulators;
    public volatile int stabilizers;
    @Nullable
    public volatile BlockPos center;

    void countTransformer() {
        workingTransformers++;
    }

    void countFluxRegulator() {
        workingFluxRegulators++;
    }

    void countStabilizer() {
        workingStabilizers++;
    }

    void setWorkingCenter(BlockPos pos) {
        workingCenter = pos.immutable();
    }

    @Override
    protected void resetWorkingData() {
        workingTransformers = 0;
        workingFluxRegulators = 0;
        workingStabilizers = 0;
        workingCenter = null;
    }

    @Override
    protected void publishData() {
        transformers = workingTransformers;
        fluxRegulators = workingFluxRegulators;
        stabilizers = workingStabilizers;
        center = workingCenter;
    }
}
