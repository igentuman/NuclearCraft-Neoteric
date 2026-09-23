package igentuman.nc.particle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public record BeamConnection(
        BlockPos source,
        Direction direction,
        int maximumReach,
        BlockPos destination
) {

    public enum Outcome {
        CONNECTED,
        OBSTRUCTED,
        WRONG_FACING,
        OUT_OF_REACH,
        UNLOADED
    }

    public record ScanResult(Outcome outcome, @Nullable BeamConnection connection) {

        private static ScanResult of(Outcome outcome) {
            return new ScanResult(outcome, null);
        }
    }

    public static ScanResult scan(
            ServerLevel level,
            BlockPos source,
            Direction direction,
            int maximumReach,
            Predicate<BlockState> particleBeam,
            Predicate<BlockState> receiverCandidate,
            Predicate<BlockState> facesTowardSource
    ) {
        if (maximumReach <= 0) {
            return ScanResult.of(Outcome.OUT_OF_REACH);
        }

        for (int step = 1; step <= maximumReach; step++) {
            BlockPos pos = source.relative(direction, step);
            if (!level.hasChunkAt(pos)) {
                return ScanResult.of(Outcome.UNLOADED);
            }
            BlockState state = level.getBlockState(pos);
            if (particleBeam.test(state)) {
                continue;
            }
            if (!receiverCandidate.test(state)) {
                return ScanResult.of(Outcome.OBSTRUCTED);
            }
            if (!facesTowardSource.test(state)) {
                return ScanResult.of(Outcome.WRONG_FACING);
            }
            return new ScanResult(Outcome.CONNECTED, new BeamConnection(source, direction, maximumReach, pos));
        }
        return ScanResult.of(Outcome.OUT_OF_REACH);
    }

    public static Predicate<BlockState> facesOpposite(Direction direction) {
        Direction expected = direction.getOpposite();
        return state -> state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == expected;
    }
}
