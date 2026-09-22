package igentuman.nc.multiblock.accelerator;

import igentuman.nc.multiblock.StructureRecord;
import igentuman.nc.multiblock.fusion.ElectromagnetDef;
import igentuman.nc.multiblock.fusion.RFAmplifierDef;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public record LinearAcceleratorValidationContext(
        StructureRecord source,
        Predicate<BlockState> cornerCasing,
        Predicate<BlockState> shell,
        Predicate<BlockState> controller,
        Predicate<BlockState> servicePort,
        Predicate<BlockState> beamPort,
        Predicate<BlockState> ionSourcePort,
        Predicate<BlockState> particleBeam,
        Predicate<BlockState> yoke,
        Function<BlockState, ElectromagnetDef> electromagnet,
        Function<BlockState, RFAmplifierDef> rfAmplifier,
        Function<BlockState, CoolerDef> cooler,
        long heatCapacityPerBlock
) {
    public LinearAcceleratorValidationContext {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(cornerCasing, "cornerCasing");
        Objects.requireNonNull(shell, "shell");
        Objects.requireNonNull(controller, "controller");
        Objects.requireNonNull(servicePort, "servicePort");
        Objects.requireNonNull(beamPort, "beamPort");
        Objects.requireNonNull(ionSourcePort, "ionSourcePort");
        Objects.requireNonNull(particleBeam, "particleBeam");
        Objects.requireNonNull(yoke, "yoke");
        Objects.requireNonNull(electromagnet, "electromagnet");
        Objects.requireNonNull(rfAmplifier, "rfAmplifier");
        Objects.requireNonNull(cooler, "cooler");
        if (heatCapacityPerBlock <= 0) throw new IllegalArgumentException("heatCapacityPerBlock must be positive");
    }
}
