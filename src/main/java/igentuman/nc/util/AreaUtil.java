package igentuman.nc.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.apache.commons.lang3.tuple.Pair;

public class AreaUtil {

    public static Pair<BlockPos, BlockPos> getArea(BlockPos pos, Direction facing, int radius, boolean depth) {
        BlockPos bottomLeft = pos.relative(facing.getAxis() == Direction.Axis.Y ? Direction.SOUTH : Direction.DOWN, radius).relative(facing.getAxis() == Direction.Axis.Y ? Direction.WEST : facing.getCounterClockWise(), radius);
        BlockPos topRight = pos.relative(facing.getAxis() == Direction.Axis.Y ? Direction.NORTH : Direction.UP, radius).relative(facing.getAxis() == Direction.Axis.Y ? Direction.EAST : facing.getClockWise(), radius);
        if (facing.getAxis() != Direction.Axis.Y && radius > 0) {
            bottomLeft = bottomLeft.relative(Direction.UP, radius - 1);
            topRight = topRight.relative(Direction.UP, radius - 1);
        }
        if (depth) {
            topRight = topRight.relative(facing.getOpposite(), radius+1);
        }
        return Pair.of(bottomLeft, topRight);
    }
}
