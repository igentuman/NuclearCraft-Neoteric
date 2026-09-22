package igentuman.nc.multiblock.geometry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Objects;

public record StructureTransform(BlockPos origin, Direction forward, Direction right, Direction up) {

    public StructureTransform {
        origin = Objects.requireNonNull(origin, "origin").immutable();
        forward = Objects.requireNonNull(forward, "forward");
        right = Objects.requireNonNull(right, "right");
        up = Objects.requireNonNull(up, "up");
        if (forward.getAxis() == right.getAxis()
                || forward.getAxis() == up.getAxis()
                || right.getAxis() == up.getAxis()) {
            throw new IllegalArgumentException("Structure transform axes must be perpendicular");
        }
    }

    public BlockPos toWorld(int localRight, int localUp, int localForward) {
        long x = coordinate(origin.getX(), right.getStepX(), localRight, up.getStepX(), localUp,
                forward.getStepX(), localForward);
        long y = coordinate(origin.getY(), right.getStepY(), localRight, up.getStepY(), localUp,
                forward.getStepY(), localForward);
        long z = coordinate(origin.getZ(), right.getStepZ(), localRight, up.getStepZ(), localUp,
                forward.getStepZ(), localForward);
        return new BlockPos(Math.toIntExact(x), Math.toIntExact(y), Math.toIntExact(z));
    }

    public LocalPosition toLocal(BlockPos worldPosition) {
        Objects.requireNonNull(worldPosition, "worldPosition");
        long dx = (long) worldPosition.getX() - origin.getX();
        long dy = (long) worldPosition.getY() - origin.getY();
        long dz = (long) worldPosition.getZ() - origin.getZ();
        return new LocalPosition(
                dot(dx, dy, dz, right),
                dot(dx, dy, dz, up),
                dot(dx, dy, dz, forward));
    }

    private static long coordinate(int origin, int firstStep, int firstDistance,
                                   int secondStep, int secondDistance, int thirdStep, int thirdDistance) {
        return Math.addExact(origin, Math.addExact(
                Math.multiplyExact((long) firstStep, firstDistance),
                Math.addExact(Math.multiplyExact((long) secondStep, secondDistance),
                        Math.multiplyExact((long) thirdStep, thirdDistance))));
    }

    private static int dot(long x, long y, long z, Direction direction) {
        long value = x * direction.getStepX() + y * direction.getStepY() + z * direction.getStepZ();
        return Math.toIntExact(value);
    }

    public record LocalPosition(int right, int up, int forward) {
    }
}
