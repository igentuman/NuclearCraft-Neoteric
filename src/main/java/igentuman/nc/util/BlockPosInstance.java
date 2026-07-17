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
public class BlockPosInstance extends BlockPos {

    public final int origX;
    public final int origY;
    public final int origZ;

    public static BlockPosInstance of(BlockPos pos) {
        if(pos instanceof BlockPosInstance)
            return (BlockPosInstance) pos;

        return new BlockPosInstance(pos.getX(), pos.getY(), pos.getZ());
    }

    public static BlockPosInstance of(long pPackedPos) {
        return new BlockPosInstance(getX(pPackedPos), getY(pPackedPos), getZ(pPackedPos));
    }


    public static BlockPosInstance copy(BlockPos pos) {
        return new BlockPosInstance(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockPosInstance(int x, int y, int z) {
        super(x, y, z);
        origX = x;
        origY = y;
        origZ = z;
    }

    public BlockPosInstance(Vec3i pos) {
        super(pos);
        origX = pos.getX();
        origY = pos.getY();
        origZ = pos.getZ();
    }

    public BlockPosInstance revert()
    {
        this.setX(origX);
        this.setY(origY);
        this.setZ(origZ);
        return this;
    }
    
    @Override
    public BlockPosInstance relative(Direction direction, int distance) {
        setX(getX()+direction.getStepX() * distance);
        setY(getY()+direction.getStepY() * distance);
        setZ(getZ()+direction.getStepZ() * distance);
        return this;
    }

    @Override
    public BlockPosInstance relative(Direction pDirection) {
        return relative(pDirection, 1);
    }

    @Override
    public BlockPosInstance offset(int x, int y, int z) {
        setX(getX()+x);
        setY(getY()+y);
        setZ(getZ()+z);
        return this;
    }

    public BlockPosInstance y(int y) {
        setY(y);
        return this;
    }

    public BlockPosInstance x(int x) {
        setX(x);
        return this;
    }

    public BlockPosInstance z(int z) {
        setZ(z);
        return this;
    }

    public BlockPos copy() {
        return new BlockPosInstance(getX(), getY(), getZ());
    }
}
