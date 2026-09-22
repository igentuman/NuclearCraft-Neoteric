package igentuman.nc.multiblock.particle_chamber;

import igentuman.nc.multiblock.StructureRecord;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public record TargetChamberValidationContext(
        StructureRecord source,
        Predicate<BlockState> cornerCasing,
        Predicate<BlockState> shell,
        Predicate<BlockState> controller,
        Predicate<BlockState> servicePort,
        Predicate<BlockState> beamPort,
        Predicate<BlockState> particleBeam,
        Predicate<BlockState> camera,
        Predicate<BlockState> innerFill,
        Function<BlockState, DetectorDef> detector
) {
    public TargetChamberValidationContext {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(cornerCasing, "cornerCasing");
        Objects.requireNonNull(shell, "shell");
        Objects.requireNonNull(controller, "controller");
        Objects.requireNonNull(servicePort, "servicePort");
        Objects.requireNonNull(beamPort, "beamPort");
        Objects.requireNonNull(particleBeam, "particleBeam");
        Objects.requireNonNull(camera, "camera");
        Objects.requireNonNull(innerFill, "innerFill");
        Objects.requireNonNull(detector, "detector");
    }
}
