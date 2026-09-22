package igentuman.nc.particle;

import igentuman.nc.multiblock.validation.LoadedStructureReader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

public record BeamConnection(
        BlockPos source,
        Direction direction,
        int maximumReach,
        BlockPos destination,
        Map<Long, Long> scannedSectionRevisions
) {

    public BeamConnection {
        scannedSectionRevisions = Map.copyOf(scannedSectionRevisions);
    }

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
            LoadedStructureReader reader,
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

        Map<Long, Long> scannedSections = new LinkedHashMap<>();
        for (int step = 1; step <= maximumReach; step++) {
            BlockPos pos = source.relative(direction, step);
            SectionPos sectionPos = SectionPos.of(pos);
            if (!reader.isLoaded(sectionPos)) {
                return ScanResult.of(Outcome.UNLOADED);
            }
            scannedSections.putIfAbsent(sectionPos.asLong(), reader.sectionRevision(sectionPos));

            if (reader.matchesBlock(pos, particleBeam)) {
                continue;
            }
            if (!reader.matchesBlock(pos, receiverCandidate)) {
                return ScanResult.of(Outcome.OBSTRUCTED);
            }
            if (!reader.matchesBlock(pos, facesTowardSource)) {
                return ScanResult.of(Outcome.WRONG_FACING);
            }
            return new ScanResult(Outcome.CONNECTED, new BeamConnection(source, direction, maximumReach, pos, scannedSections));
        }
        return ScanResult.of(Outcome.OUT_OF_REACH);
    }

    public static Predicate<BlockState> facesOpposite(Direction direction) {
        Direction expected = direction.getOpposite();
        return state -> state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == expected;
    }

    public boolean isStale(LoadedStructureReader reader) {
        for (Map.Entry<Long, Long> entry : scannedSectionRevisions.entrySet()) {
            SectionPos sectionPos = SectionPos.of(entry.getKey());
            if (!reader.isLoaded(sectionPos) || reader.sectionRevision(sectionPos) != entry.getValue()) {
                return true;
            }
        }
        return false;
    }
}
