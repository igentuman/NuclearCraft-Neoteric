package igentuman.nc.util.functions;

import igentuman.nc.util.NBTConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;

public class Coord4D {

    private final int x;
    private final int y;
    private final int z;
    public final ResourceKey<Level> dimension;
    private final int hashCode;

    public Coord4D(Entity entity) {
        this(entity.blockPosition(), entity.level);
    }

    public Coord4D(double x, double y, double z, ResourceKey<Level> dimension) {
        this.x = Mth.floor(x);
        this.y = Mth.floor(y);
        this.z = Mth.floor(z);
        this.dimension = dimension;
        this.hashCode = initHashCode();
    }

    public Coord4D(Vec3i pos, Level world) {
        this(pos, world.dimension());
    }

    public Coord4D(Vec3i pos, ResourceKey<Level> dimension) {
        this(pos.getX(), pos.getY(), pos.getZ(), dimension);
    }

    public Coord4D(BlockEntity tile) {
        this(tile.getBlockPos(), Objects.requireNonNull(tile.getLevel(), "Block entity has no level."));
    }

    public static Coord4D read(CompoundTag tag) {
        return new Coord4D(tag.getInt(NBTConstants.X), tag.getInt(NBTConstants.Y), tag.getInt(NBTConstants.Z),
              ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString(NBTConstants.DIMENSION))));
    }

    public static Coord4D read(FriendlyByteBuf dataStream) {
        return new Coord4D(dataStream.readBlockPos(), ResourceKey.create(Registry.DIMENSION_REGISTRY, dataStream.readResourceLocation()));
    }

    /**
     * Gets the X coordinate.
     */
    public int getX() {
        return this.x;
    }

    /**
     * Gets the Y coordinate.
     */
    public int getY() {
        return this.y;
    }

    /**
     * Gets the Z coordinate.
     */
    public int getZ() {
        return this.z;
    }

    public BlockPos getPos() {
        return new BlockPos(x, y, z);
    }

    public CompoundTag write(CompoundTag nbtTags) {
        nbtTags.putInt(NBTConstants.X, x);
        nbtTags.putInt(NBTConstants.Y, y);
        nbtTags.putInt(NBTConstants.Z, z);
        nbtTags.putString(NBTConstants.DIMENSION, dimension.location().toString());
        return nbtTags;
    }

    public void write(FriendlyByteBuf dataStream) {
        //Note: We write the position as a block pos over the network so that it can be packed more efficiently
        dataStream.writeBlockPos(getPos());
        dataStream.writeResourceLocation(dimension.location());
    }

    public Coord4D translate(int x, int y, int z) {
        return new Coord4D(this.x + x, this.y + y, this.z + z, dimension);
    }

    public Coord4D offset(Direction side) {
        return offset(side, 1);
    }

    public Coord4D offset(Direction side, int amount) {
        if (side == null || amount == 0) {
            return this;
        }
        return new Coord4D(x + (side.getStepX() * amount), y + (side.getStepY() * amount), z + (side.getStepZ() * amount), dimension);
    }

    public double distanceTo(Coord4D obj) {
        return Math.sqrt(distanceToSquared(obj));
    }

    public double distanceToSquared(Coord4D obj) {
        int subX = x - obj.x;
        int subY = y - obj.y;
        int subZ = z - obj.z;
        return subX * subX + subY * subY + subZ * subZ;
    }

    @Override
    public String toString() {
        return "[Coord4D: " + x + ", " + y + ", " + z + ", dim=" + dimension.location() + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Coord4D other && other.x == x && other.y == y && other.z == z && other.dimension == dimension;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    private int initHashCode() {
        int code = 1;
        code = 31 * code + x;
        code = 31 * code + y;
        code = 31 * code + z;
        return 31 * code + dimension.hashCode();
    }
}