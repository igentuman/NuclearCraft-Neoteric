package igentuman.nc.multiblock.accelerator;

import igentuman.nc.multiblock.StructureRecord;
import igentuman.nc.multiblock.fusion.ElectromagnetDef;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public record BeamDiverterValidationContext(
        StructureRecord source,
        Predicate<BlockState> cornerCasing,
        Predicate<BlockState> shell,
        Predicate<BlockState> controller,
        Predicate<BlockState> servicePort,
        Predicate<BlockState> beamPort,
        Predicate<BlockState> particleBeam,
        Predicate<BlockState> yoke,
        Function<BlockState, ElectromagnetDef> electromagnet,
        long heatCapacityPerBlock
) {
    public BeamDiverterValidationContext {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(cornerCasing, "cornerCasing");
        Objects.requireNonNull(shell, "shell");
        Objects.requireNonNull(controller, "controller");
        Objects.requireNonNull(servicePort, "servicePort");
        Objects.requireNonNull(beamPort, "beamPort");
        Objects.requireNonNull(particleBeam, "particleBeam");
        Objects.requireNonNull(yoke, "yoke");
        Objects.requireNonNull(electromagnet, "electromagnet");
        if (heatCapacityPerBlock <= 0) throw new IllegalArgumentException("heatCapacityPerBlock must be positive");
    }
}
