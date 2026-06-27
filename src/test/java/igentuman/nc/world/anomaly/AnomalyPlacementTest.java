package igentuman.nc.world.anomaly;

import igentuman.nc.entity.anomaly.AnomalyType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnomalyPlacementTest {

    private static final int CELL = 256;
    private static final long SEED = 0x1234_5678_9ABC_DEF0L;
    private static final int[] EQUAL_WEIGHTS = {1, 1, 1, 1, 1, 1};

    @Test
    void cellIndexFloorsTowardNegativeInfinity() {
        assertEquals(0, AnomalyPlacement.cellIndex(0, CELL));
        assertEquals(0, AnomalyPlacement.cellIndex(255, CELL));
        assertEquals(1, AnomalyPlacement.cellIndex(256, CELL));
        assertEquals(-1, AnomalyPlacement.cellIndex(-1, CELL));
        assertEquals(-1, AnomalyPlacement.cellIndex(-256, CELL));
        assertEquals(-2, AnomalyPlacement.cellIndex(-257, CELL));
    }

    @Test
    void placementIsDeterministicForSameSeedAndCell() {
        Optional<AnomalyPlacement.PlannedAnomaly> a =
                AnomalyPlacement.forCell(SEED, 3, -7, 1.0D, CELL, EQUAL_WEIGHTS);
        Optional<AnomalyPlacement.PlannedAnomaly> b =
                AnomalyPlacement.forCell(SEED, 3, -7, 1.0D, CELL, EQUAL_WEIGHTS);
        assertEquals(a, b);
        assertTrue(a.isPresent());
    }

    @Test
    void mixDecorrelatesSwappedCoordinates() {
        // The legacy "seed + x + z" pattern was symmetric in x/z; the SplitMix mix must not be.
        assertNotEquals(AnomalyPlacement.mix(SEED, 5, 9), AnomalyPlacement.mix(SEED, 9, 5));
    }

    @Test
    void plannedCoordinatesLandInsideTheirCell() {
        for (int cx = -4; cx <= 4; cx++) {
            for (int cz = -4; cz <= 4; cz++) {
                Optional<AnomalyPlacement.PlannedAnomaly> planned =
                        AnomalyPlacement.forCell(SEED, cx, cz, 1.0D, CELL, EQUAL_WEIGHTS);
                if (planned.isEmpty()) {
                    continue;
                }
                AnomalyPlacement.PlannedAnomaly p = planned.get();
                assertEquals(cx, AnomalyPlacement.cellIndex(p.x(), CELL));
                assertEquals(cz, AnomalyPlacement.cellIndex(p.z(), CELL));
                assertEquals(AnomalyPlacement.cellId(cx, cz), p.cellId());
            }
        }
    }

    @Test
    void zeroSpawnChanceProducesNothing() {
        assertTrue(AnomalyPlacement.forCell(SEED, 0, 0, 0.0D, CELL, EQUAL_WEIGHTS).isEmpty());
    }

    @Test
    void fullSpawnChanceAlwaysProducesAnomaly() {
        for (int cx = -10; cx <= 10; cx++) {
            assertTrue(AnomalyPlacement.forCell(SEED, cx, 0, 1.0D, CELL, EQUAL_WEIGHTS).isPresent());
        }
    }

    @Test
    void weightedPickReturnsNullWhenAllWeightsZero() {
        assertNull(AnomalyPlacement.weightedPick(new Random(1), new int[]{0, 0, 0, 0, 0, 0}));
    }

    @Test
    void weightedPickHonoursZeroWeightVariants() {
        // Only RADIOACTIVE (ordinal 2) enabled -> every pick must be RADIOACTIVE.
        int[] onlyRadioactive = {0, 0, 5, 0, 0, 0};
        Random rng = new Random(42);
        for (int i = 0; i < 1000; i++) {
            assertEquals(AnomalyType.RADIOACTIVE, AnomalyPlacement.weightedPick(rng, onlyRadioactive));
        }
    }

    @Test
    void distributionRoughlyTracksWeights() {
        // GRAVITATIONAL weight 3, everything else 1: gravitational should dominate.
        int[] weights = {3, 1, 1, 1, 1, 1};
        Map<AnomalyType, Integer> counts = new HashMap<>();
        Random rng = new Random(SEED);
        int samples = 60_000;
        for (int i = 0; i < samples; i++) {
            AnomalyType type = AnomalyPlacement.weightedPick(rng, weights);
            counts.merge(type, 1, Integer::sum);
        }
        int grav = counts.getOrDefault(AnomalyType.GRAVITATIONAL, 0);
        int electric = counts.getOrDefault(AnomalyType.ELECTRIC, 0);
        assertFalse(grav == 0);
        // Expected ratio ~3:1; allow generous slack for randomness.
        assertTrue(grav > electric * 2, "gravitational (" + grav + ") should clearly outnumber electric (" + electric + ")");
    }
}
