package igentuman.nc.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

/**
 * Wrapper class for BlockPos manipulations
 * Helps to avoid instancing of new BlockPos objects
 * instead of instancing of new BlockPos it changes x, y, z of this object
 * keeps track of changes in x, y, z and reverts them back
 */
public class NCBlockPos extends BlockPos {

    public final int origX;
    public final int origY;
    public final int origZ;

    public static NCBlockPos of(BlockPos pos) {
        if(pos instanceof NCBlockPos)
            return (NCBlockPos) pos;

        return new NCBlockPos(pos.getX(), pos.getY(), pos.getZ());
    }

    public NCBlockPos(int x, int y, int z) {
        super(x, y, z);
        origX = x;
        origY = y;
        origZ = z;
    }

    public NCBlockPos(Vec3i pos) {
        super(pos);
        origX = pos.getX();
        origY = pos.getY();
        origZ = pos.getZ();
    }

    public NCBlockPos revert()
    {
        this.setX(origX);
        this.setY(origY);
        this.setZ(origZ);
        return this;
    }
    
    @Override
    public NCBlockPos relative(Direction direction, int distance) {
        setX(getX()+direction.getStepX() * distance);
        setY(getY()+direction.getStepY() * distance);
        setZ(getZ()+direction.getStepZ() * distance);
        return this;
    }

    @Override
    public NCBlockPos relative(Direction pDirection) {
        return relative(pDirection, 1);
    }

    @Override
    public NCBlockPos offset(int x, int y, int z) {
        setX(getX()+x);
        setY(getY()+y);
        setZ(getZ()+z);
        return this;
    }
}
