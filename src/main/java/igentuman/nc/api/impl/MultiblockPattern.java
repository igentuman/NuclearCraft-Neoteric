package igentuman.nc.api.impl;

import igentuman.nc.api.multiblock.BlockPredicate;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 3D pattern of {@link BlockPredicate}s indexed as [y][z][x].
 *
 * <p>NBT format stores width/height/depth, controllerOffset, and a flat list of predicate
 * registry-key strings in [y][z][x] order. Custom predicates must be registered via
 * {@link #registerPredicate} before world load to round-trip.
 */
public class MultiblockPattern {

    private static final Map<String, BlockPredicate> REGISTRY = new HashMap<>();
    private static final Map<BlockPredicate, String> REVERSE = new HashMap<>();

    static {
        registerPredicate("any", BlockPredicate.any());
        registerPredicate("air", BlockPredicate.air());
    }

    public static void registerPredicate(String key, BlockPredicate predicate) {
        REGISTRY.put(key, predicate);
        REVERSE.put(predicate, key);
    }

    public final int width;
    public final int height;
    public final int depth;
    public final Vec3i controllerOffset;
    private final BlockPredicate[][][] cells;

    public MultiblockPattern(int width, int height, int depth, Vec3i controllerOffset, BlockPredicate[][][] cells) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.controllerOffset = controllerOffset;
        this.cells = cells;
    }

    public BlockPredicate get(int x, int y, int z) {
        return cells[y][z][x];
    }

    public static Builder builder(int width, int height, int depth) {
        return new Builder(width, height, depth);
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("w", width);
        tag.putInt("h", height);
        tag.putInt("d", depth);
        tag.putInt("ox", controllerOffset.getX());
        tag.putInt("oy", controllerOffset.getY());
        tag.putInt("oz", controllerOffset.getZ());
        ListTag list = new ListTag();
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < depth; z++) {
                for (int x = 0; x < width; x++) {
                    BlockPredicate p = cells[y][z][x];
                    String key = REVERSE.getOrDefault(p, "any");
                    list.add(StringTag.valueOf(key));
                }
            }
        }
        tag.put("cells", list);
        return tag;
    }

    public static MultiblockPattern fromNBT(CompoundTag tag) {
        int w = tag.getInt("w");
        int h = tag.getInt("h");
        int d = tag.getInt("d");
        Vec3i offset = new Vec3i(tag.getInt("ox"), tag.getInt("oy"), tag.getInt("oz"));
        ListTag list = tag.getList("cells", 8);
        BlockPredicate[][][] cells = new BlockPredicate[h][d][w];
        int idx = 0;
        for (int y = 0; y < h; y++) {
            for (int z = 0; z < d; z++) {
                for (int x = 0; x < w; x++) {
                    String key = list.getString(idx++);
                    BlockPredicate p = REGISTRY.get(key);
                    cells[y][z][x] = Objects.requireNonNullElse(p, BlockPredicate.any());
                }
            }
        }
        return new MultiblockPattern(w, h, d, offset, cells);
    }

    public static class Builder {
        private final int width;
        private final int height;
        private final int depth;
        private final BlockPredicate[][][] cells;
        private Vec3i controllerOffset = Vec3i.ZERO;

        public Builder(int width, int height, int depth) {
            this.width = width;
            this.height = height;
            this.depth = depth;
            this.cells = new BlockPredicate[height][depth][width];
            for (int y = 0; y < height; y++)
                for (int z = 0; z < depth; z++)
                    for (int x = 0; x < width; x++)
                        cells[y][z][x] = BlockPredicate.any();
        }

        public Builder set(int x, int y, int z, BlockPredicate predicate) {
            cells[y][z][x] = predicate;
            return this;
        }

        public Builder fillLayer(int y, BlockPredicate predicate) {
            for (int z = 0; z < depth; z++)
                for (int x = 0; x < width; x++)
                    cells[y][z][x] = predicate;
            return this;
        }

        public Builder controllerOffset(int x, int y, int z) {
            this.controllerOffset = new Vec3i(x, y, z);
            return this;
        }

        public MultiblockPattern build() {
            return new MultiblockPattern(width, height, depth, controllerOffset, cells);
        }
    }
}
