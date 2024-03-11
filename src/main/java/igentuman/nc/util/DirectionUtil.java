package igentuman.nc.util;

import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;

import java.util.Arrays;
import java.util.Comparator;

import static net.minecraft.util.Direction.*;


public class DirectionUtil
{
	public static final Direction[] VALUES = Direction.values();
	public static final Direction[] BY_HORIZONTAL_INDEX = Arrays.stream(VALUES)
			.filter((direction) -> direction.getAxis().isHorizontal())
			.sorted(Comparator.comparingInt(Direction::get2DDataValue))
			.toArray(Direction[]::new);

	public static Rotation getRotationBetweenFacings(Direction orig, Direction to)
	{
		if(to==orig)
			return Rotation.NONE;
		if(orig.getAxis()== Direction.Axis.Y||to.getAxis()== Direction.Axis.Y)
			return null;
		orig = orig.getClockWise();
		if(orig==to)
			return Rotation.CLOCKWISE_90;
		orig = orig.getClockWise();
		if(orig==to)
			return Rotation.CLOCKWISE_180;
		orig = orig.getClockWise();
		if(orig==to)
			return Rotation.COUNTERCLOCKWISE_90;
		return null;//This shouldn't ever happen
	}

	public static Direction rotateAround(Direction d, Direction.Axis axis)
	{
		if (axis == d.getAxis())
			return d;
		switch(axis)
				{
					case X:
						return rotateX(d);
					case Y:
						return d.getClockWise();
					case Z:
						return rotateZ(d);
				};
		return null;
	}

	public static Direction rotateX(Direction d)
	{
		switch(d)
				{
					case NORTH: return DOWN;
					case SOUTH: return UP;
					case UP: return NORTH;
					case DOWN: return SOUTH;
				//	case EAST, WEST -> throw new IllegalStateException("Unable to get X-rotated facing of "+d);
				};
		return null;
	}

	public static Direction rotateZ(Direction d)
	{
		switch(d)
				{
					case EAST: return DOWN;
					case WEST: return UP;
					case UP: return EAST;
					case DOWN: return WEST;
					//case SOUTH, NORTH -> throw new IllegalStateException("Unable to get Z-rotated facing of "+d);
				};
		return null;
	}
}
