package igentuman.nc.multiblock.turbine;

import igentuman.nc.api.multiblock.part.TurbineCoilDef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;

public final class TurbineCoilValidator {

    private TurbineCoilValidator() {}

    private static final Direction[] HORIZONTAL = {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };

    public static boolean isValid(TurbineCoilDef def, BlockPos pos, TurbineCache cache) {
        Map<String[], List<Block>> conditions = def.getValidator().blocks();
        if (conditions.isEmpty()) return true;
        for (Map.Entry<String[], List<Block>> e : conditions.entrySet()) {
            String func = e.getKey()[0];
            int count = parseCount(e.getKey()[1]);
            List<Block> blocks = e.getValue();
            boolean ok = switch (func) {
                case "<" -> isLessThan(count, blocks, pos, cache);
                case "=" -> isExact(count, blocks, pos, cache);
                case "-" -> isBetween(blocks, pos, cache);
                case "^" -> inCorner(count, blocks, pos, cache);
                default -> isAtLeast(count, blocks, pos, cache);
            };
            if (!ok) return false;
        }
        return true;
    }

    private static int parseCount(String s) {
        try {
            return Math.max(Integer.parseInt(s), 1);
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }

    private static boolean isCoil(Block b) {
        ResourceLocation rl = BuiltInRegistries.BLOCK.getKey(b);
        String path = rl.getPath();
        return path.startsWith("turbine_") && path.endsWith("_coil");
    }

    private static boolean qualifies(List<Block> blocks, BlockPos pos, TurbineCache cache) {
        if (!withinBounds(pos, cache)) return false;
        BlockState bs = cache.getBlockState(pos);
        Block b = bs.getBlock();
        if (!blocks.contains(b)) return false;
        if (isCoil(b) && !cache.workingValidCoils.contains(pos.asLong())) return false;
        return true;
    }

    private static boolean withinBounds(BlockPos pos, TurbineCache cache) {
        BlockPos min = cache.workingMin();
        BlockPos max = cache.workingMax();
        if (min == null || max == null) return false;
        return pos.getX() >= min.getX() && pos.getX() <= max.getX()
                && pos.getY() >= min.getY() && pos.getY() <= max.getY()
                && pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
    }

    private static boolean isAtLeast(int n, List<Block> blocks, BlockPos pos, TurbineCache cache) {
        int c = 0;
        for (Direction dir : Direction.values()) {
            if (qualifies(blocks, pos.relative(dir), cache) && ++c >= n) return true;
        }
        return c >= n;
    }

    private static boolean isLessThan(int n, List<Block> blocks, BlockPos pos, TurbineCache cache) {
        int c = 0;
        for (Direction dir : Direction.values()) {
            if (qualifies(blocks, pos.relative(dir), cache) && ++c >= n) return false;
        }
        return c < n;
    }

    private static boolean isExact(int n, List<Block> blocks, BlockPos pos, TurbineCache cache) {
        int c = 0;
        for (Direction dir : Direction.values()) {
            if (qualifies(blocks, pos.relative(dir), cache) && ++c > n) return false;
        }
        return c == n;
    }

    private static boolean isBetween(List<Block> blocks, BlockPos pos, TurbineCache cache) {
        for (Direction dir : Direction.values()) {
            if (qualifies(blocks, pos.relative(dir), cache)
                    && qualifies(blocks, pos.relative(dir.getOpposite()), cache)) {
                return true;
            }
        }
        return false;
    }

    private static boolean inCorner(int qty, List<Block> blocks, BlockPos pos, TurbineCache cache) {
        int vertical = qualifies(blocks, pos.above(), cache) ? 1 : 0;
        if (qualifies(blocks, pos.below(), cache)) vertical = 1;
        int[] m = new int[4];
        for (int i = 0; i < 4; i++) {
            if (qualifies(blocks, pos.relative(HORIZONTAL[i]), cache)) {
                if (1 + vertical >= qty) return true;
                m[i] = 1;
            }
        }
        for (int k = 0; k < 4; k++) {
            int next = (k + 1) % 4;
            if (m[k] + m[next] + vertical >= qty) return true;
        }
        return false;
    }
}
