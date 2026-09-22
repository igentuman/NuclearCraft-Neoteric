package igentuman.nc.multiblock.discovery;

import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;
import java.util.function.Predicate;

public record BoundaryDiscoverySpec(
        Shape shape,
        int minimumRight,
        int maximumRight,
        int minimumHeight,
        int maximumHeight,
        int minimumDepth,
        int maximumDepth,
        int wallOffset,
        int minimumLongAxis,
        Predicate<BlockState> shellPredicate
) {
    public BoundaryDiscoverySpec {
        Objects.requireNonNull(shape, "shape");
        Objects.requireNonNull(shellPredicate, "shellPredicate");
        requireRange("right", minimumRight, maximumRight);
        requireRange("height", minimumHeight, maximumHeight);
        requireRange("depth", minimumDepth, maximumDepth);
        if (wallOffset < 0) throw new IllegalArgumentException("wallOffset must be nonnegative");
        if (minimumLongAxis < 1) throw new IllegalArgumentException("minimumLongAxis must be positive");
        if (shape == Shape.SQUARE_RING && wallOffset < 1) {
            throw new IllegalArgumentException("A ring requires a positive wallOffset");
        }
    }

    public BoundaryDiscoverySpec(Shape shape, int minimumRight, int maximumRight, int minimumHeight,
                                 int maximumHeight, int minimumDepth, int maximumDepth, int wallOffset,
                                 Predicate<BlockState> shellPredicate) {
        this(shape, minimumRight, maximumRight, minimumHeight, maximumHeight, minimumDepth, maximumDepth,
                wallOffset, 1, shellPredicate);
    }

    private static void requireRange(String name, int minimum, int maximum) {
        if (minimum < 1 || maximum < minimum) {
            throw new IllegalArgumentException("Invalid " + name + " range");
        }
    }

    public enum Shape {
        BOX,
        ODD_CUBE,
        COLLISION_BOX,
        LINEAR_TUBE,
        SQUARE_RING
    }
}
