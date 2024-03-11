package igentuman.nc.util.functions;

import igentuman.nc.util.NBTConstants;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Objects;

public class Coord4D {

    private final int x;
    private final int y;
    private final int z;
    public final RegistryKey<World> dimension;
    private final int hashCode;

    public Coord4D(Entity entity) {
        this(entity.blockPosition(), entity.level);
    }

    public Coord4D(double x, double y, double z, RegistryKey<World> dimension) {
        this.x = MathHelper.floor(x);
        this.y = MathHelper.floor(y);
        this.z = MathHelper.floor(z);
        this.dimension = dimension;
        this.hashCode = initHashCode();
    }

    public Coord4D(BlockPos pos, World world) {
        this(pos, world.dimension());
    }

    public Coord4D(BlockPos pos, RegistryKey<World> dimension) {
        this(pos.getX(), pos.getY(), pos.getZ(), dimension);
    }

    public Coord4D(TileEntity tile) {
        this(tile.getBlockPos(), Objects.requireNonNull(tile.getLevel(), "Block entity has no level."));
    }

    public static Coord4D read(CompoundNBT tag) {
        return new Coord4D(tag.getInt(NBTConstants.X), tag.getInt(NBTConstants.Y), tag.getInt(NBTConstants.Z),
              RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString(NBTConstants.DIMENSION))));
    }

    public static Coord4D read(PacketBuffer dataStream) {
        return new Coord4D(dataStream.readBlockPos(), RegistryKey.create(Registry.DIMENSION_REGISTRY, dataStream.readResourceLocation()));
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

    public CompoundNBT write(CompoundNBT nbtTags) {
        nbtTags.putInt(NBTConstants.X, x);
        nbtTags.putInt(NBTConstants.Y, y);
        nbtTags.putInt(NBTConstants.Z, z);
        nbtTags.putString(NBTConstants.DIMENSION, dimension.location().toString());
        return nbtTags;
    }

    public void write(PacketBuffer dataStream) {
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
        return obj instanceof Coord4D && ((Coord4D) obj).x == x && ((Coord4D) obj).y == y && ((Coord4D) obj).z == z && ((Coord4D) obj).dimension == dimension;

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