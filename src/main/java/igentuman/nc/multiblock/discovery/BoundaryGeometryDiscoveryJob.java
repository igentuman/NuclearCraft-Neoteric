package igentuman.nc.multiblock.discovery;

import igentuman.nc.api.multiblock.StructureFootprint;
import igentuman.nc.multiblock.geometry.BoxFootprint;
import igentuman.nc.multiblock.geometry.LinearTubeFootprint;
import igentuman.nc.multiblock.geometry.SquareRingFootprint;
import igentuman.nc.multiblock.geometry.StructureRole;
import igentuman.nc.multiblock.geometry.StructureTransform;
import igentuman.nc.multiblock.MultiblockDebug;
import igentuman.nc.multiblock.validation.LoadedStructureReader;
import igentuman.nc.multiblock.validation.ValidationBudget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class BoundaryGeometryDiscoveryJob implements GeometryDiscoveryJob {

    private final UUID structureId;
    private final BlockPos controllerPos;
    private final Direction outward;
    private final Direction inward;
    private final Direction right;
    private final BoundaryDiscoverySpec spec;
    private final Set<Long> watchedSections = new LinkedHashSet<>();
    private Phase phase = Phase.ORIGIN;
    private int distance;
    private int down;
    private int up;
    private int left;
    private int rightExtent;
    private long completedReads;
    private boolean finished;

    public BoundaryGeometryDiscoveryJob(UUID structureId, BlockPos controllerPos, Direction facing,
                                        BoundaryDiscoverySpec spec) {
        if (structureId == null || controllerPos == null || facing == null || spec == null) {
            throw new IllegalArgumentException("Discovery fields are required");
        }
        if (!facing.getAxis().isHorizontal()) {
            throw new IllegalArgumentException("Controller facing must be horizontal");
        }
        this.structureId = structureId;
        this.controllerPos = controllerPos.immutable();
        this.outward = facing;
        this.inward = facing.getOpposite();
        this.right = facing.getCounterClockWise();
        this.spec = spec;
    }

    @Override
    public UUID structureId() {
        return structureId;
    }

    @Override
    public GeometryDiscoveryResult advance(LoadedStructureReader reader, ValidationBudget budget) {
        if (finished) return result(GeometryDiscoveryResult.Status.CANCELLED,
                "multiblock.discovery.job_finished", null, null, null);
        long started = System.nanoTime();
        int readsThisAdvance = 0;
        while (readsThisAdvance < budget.maximumBlockReads()
                && System.nanoTime() - started < budget.maximumNanos()) {
            BlockPos probe = probePosition();
            SectionPos section = SectionPos.of(probe);
            if (!reader.isLoaded(section)) {
                return result(GeometryDiscoveryResult.Status.WAITING_FOR_CHUNK,
                        "multiblock.discovery.waiting_for_chunk", null, section, null);
            }
            watchedSections.add(section.asLong());
            boolean shell = reader.matchesBlock(probe, spec.shellPredicate());
            readsThisAdvance++;
            completedReads++;
            if (phase == Phase.ORIGIN) {
                if (!shell) {
                    return invalid("multiblock.discovery.controller_not_on_shell", probe);
                }
                phase = Phase.DOWN;
                distance = 1;
                continue;
            }
            if (shell) {
                if (distance >= phaseLimit()) {
                    BlockPos beyond = probe.relative(phaseDirection());
                    return invalid("multiblock.discovery.too_large", beyond);
                }
                setExtent(distance);
                distance++;
                continue;
            }
            if (phase == Phase.DEPTH) {
                return complete(distance);
            }
            phase = phase.next();
            distance = 1;
        }
        return result(GeometryDiscoveryResult.Status.IN_PROGRESS,
                "multiblock.discovery.phase_" + phase.name().toLowerCase(java.util.Locale.ROOT),
                null, null, null);
    }

    private BlockPos probePosition() {
        return switch (phase) {
            case ORIGIN -> controllerPos;
            case DOWN -> controllerPos.below(distance);
            case UP -> controllerPos.above(distance);
            case LEFT -> controllerPos.relative(right.getOpposite(), distance);
            case RIGHT -> controllerPos.relative(right, distance);
            case DEPTH -> frontBottomLeft().relative(inward, distance);
        };
    }

    private Direction phaseDirection() {
        return switch (phase) {
            case DOWN -> Direction.DOWN;
            case UP -> Direction.UP;
            case LEFT -> right.getOpposite();
            case RIGHT -> right;
            case DEPTH -> inward;
            case ORIGIN -> outward;
        };
    }

    private int phaseLimit() {
        return switch (phase) {
            case DOWN, UP -> spec.maximumHeight();
            case LEFT, RIGHT -> spec.shape() == BoundaryDiscoverySpec.Shape.COLLISION_BOX
                    ? Math.max(spec.maximumRight(), spec.maximumDepth()) : spec.maximumRight();
            case DEPTH -> spec.maximumDepth();
            case ORIGIN -> 1;
        };
    }

    private void setExtent(int value) {
        switch (phase) {
            case DOWN -> down = value;
            case UP -> up = value;
            case LEFT -> left = value;
            case RIGHT -> rightExtent = value;
            case ORIGIN, DEPTH -> { }
        }
    }

    private BlockPos frontBottomLeft() {
        return controllerPos.below(down).relative(right.getOpposite(), left);
    }

    private GeometryDiscoveryResult complete(int depth) {
        int width = Math.addExact(Math.addExact(left, rightExtent), 1);
        int height = Math.addExact(Math.addExact(down, up), 1);
        MultiblockDebug.log("discovery STEP controller={} shape={} extents down={} up={} left={} right={} depth={} dimensions={}x{}x{}",
                controllerPos.toShortString(), spec.shape(), down, up, left, rightExtent, depth,
                width, height, depth);
        String dimensionFailure = dimensionFailure(width, height, depth);
        if (dimensionFailure != null) return invalid(dimensionFailure, controllerPos);

        StructureTransform boxTransform = new StructureTransform(frontBottomLeft(), inward, right, Direction.UP);
        Map<StructureRole, List<BlockPos>> roles = Map.of(StructureRole.CONTROLLER, List.of(controllerPos));
        StructureFootprint footprint;
        try {
            footprint = buildFootprint(boxTransform, width, height, depth, roles);
        } catch (ArithmeticException | IllegalArgumentException exception) {
            return invalid("multiblock.discovery.invalid_geometry", controllerPos);
        }
        finished = true;
        return result(GeometryDiscoveryResult.Status.VALID, "multiblock.discovery.valid",
                null, null, footprint);
    }

    private String dimensionFailure(int width, int height, int depth) {
        // Collision controllers may be on an end wall or a long side wall.
        if (spec.shape() == BoundaryDiscoverySpec.Shape.COLLISION_BOX
                && depth > 4 && depth < spec.minimumDepth()) {
            int transverse = depth;
            depth = width;
            width = transverse;
        }
        if (width < spec.minimumRight() || width > spec.maximumRight()
                || height < spec.minimumHeight() || height > spec.maximumHeight()
                || depth < spec.minimumDepth() || depth > spec.maximumDepth()) {
            return "multiblock.discovery.wrong_size";
        }
        return switch (spec.shape()) {
            case BOX -> null;
            case ODD_CUBE -> width == height && height == depth && (width & 1) == 1
                    ? null : "multiblock.discovery.expected_odd_cube";
            case COLLISION_BOX -> width == height
                    ? null : "multiblock.discovery.expected_collision_box";
            case LINEAR_TUBE -> height == 5 && (width == 5 || depth == 5)
                    && Math.max(width, depth) >= spec.minimumLongAxis()
                    ? null : "multiblock.discovery.expected_linear_tube";
            case SQUARE_RING -> height == 5 && width == depth
                    && width > spec.wallOffset() * 2
                    ? null : "multiblock.discovery.expected_square_ring";
        };
    }

    private StructureFootprint buildFootprint(StructureTransform transform, int width, int height, int depth,
                                              Map<StructureRole, List<BlockPos>> roles) {
        return switch (spec.shape()) {
            case BOX, ODD_CUBE -> new BoxFootprint(transform, width, height, depth, roles);
            case COLLISION_BOX -> {
                if (depth > 4 && depth < spec.minimumDepth()) {
                    StructureTransform rotated = new StructureTransform(transform.origin(), transform.right(),
                            transform.forward(), transform.up());
                    yield new BoxFootprint(rotated, depth, height, width, roles);
                }
                yield new BoxFootprint(transform, width, height, depth, roles);
            }
            case SQUARE_RING -> new SquareRingFootprint(transform, width, height, spec.wallOffset(), roles);
            case LINEAR_TUBE -> {
                if (width == 5) {
                    yield new LinearTubeFootprint(transform, depth, 5, roles);
                }
                StructureTransform rotated = new StructureTransform(transform.origin(), transform.right(),
                        transform.forward(), transform.up());
                yield new LinearTubeFootprint(rotated, width, 5, roles);
            }
        };
    }

    private GeometryDiscoveryResult invalid(String key, BlockPos position) {
        MultiblockDebug.log("discovery FAIL controller={} phase={} diagnostic={} pos={} distance={}",
                controllerPos.toShortString(), phase, key, position.toShortString(), distance);
        finished = true;
        return result(GeometryDiscoveryResult.Status.INVALID, key, position, null, null);
    }

    private GeometryDiscoveryResult result(GeometryDiscoveryResult.Status status, String key, BlockPos position,
                                           SectionPos waitingSection, StructureFootprint footprint) {
        return new GeometryDiscoveryResult(status, key, position, waitingSection, footprint, completedReads);
    }

    private enum Phase {
        ORIGIN,
        DOWN,
        UP,
        LEFT,
        RIGHT,
        DEPTH;

        private Phase next() {
            return values()[ordinal() + 1];
        }
    }
}
