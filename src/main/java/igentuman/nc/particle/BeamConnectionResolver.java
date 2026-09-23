package igentuman.nc.particle;

import igentuman.nc.api.particle.IParticleHandler;
import igentuman.nc.api.particle.ParticleCapabilities;
import igentuman.nc.block.accelerator.AcceleratorBeamPortBlock;
import igentuman.nc.block.particle.ParticleChamberBeamPortBlock;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public final class BeamConnectionResolver {

    private BeamConnectionResolver() {
    }

    @Nullable
    public static BeamConnection resolve(ServerLevel level, BlockPos source, Direction direction, int maximumReach) {
        var beamEntry = ModEntries.get("particle_beam");
        if (beamEntry == null || !beamEntry.hasBlock()) return null;
        return BeamConnection.scan(level, source, direction, maximumReach,
                state -> state.is(beamEntry.block().get()),
                state -> state.getBlock() instanceof AcceleratorBeamPortBlock
                        || state.getBlock() instanceof ParticleChamberBeamPortBlock,
                BeamConnection.facesOpposite(direction)).connection();
    }

    @Nullable
    public static IParticleHandler destination(ServerLevel level, BeamConnection connection) {
        return level.getCapability(ParticleCapabilities.BLOCK, connection.destination(),
                connection.direction().getOpposite());
    }

    public static int corridorLength(BeamConnection connection) {
        return Math.max(0, manhattan(connection.source(), connection.destination()) - 1);
    }

    private static int manhattan(BlockPos first, BlockPos second) {
        return Math.addExact(Math.abs(Math.subtractExact(first.getX(), second.getX())),
                Math.addExact(Math.abs(Math.subtractExact(first.getY(), second.getY())),
                        Math.abs(Math.subtractExact(first.getZ(), second.getZ()))));
    }
}
