package igentuman.nc.multiblock.turbine;

import igentuman.nc.api.multiblock.IMultiblockCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TurbineCoilValidator {

    private TurbineCoilValidator() {}

    private static final Direction[] HORIZONTAL = {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };

    public static boolean isValid(TurbineCoilDef def, Level level, BlockPos pos,
                                  IMultiblockCache cache, Set<Long> validCoils) {
        Map<String[], List<Block>> conditions = def.getValidator().blocks();
        if (conditions.isEmpty()) return true;
        for (Map.Entry<String[], List<Block>> e : conditions.entrySet()) {
            String func = e.getKey()[0];
            int count = parseCount(e.getKey()[1]);
            List<Block> blocks = e.getValue();
            boolean ok = switch (func) {
                case "<" -> isLessThan(count, blocks, level, pos, cache, validCoils);
                case "=" -> isExact(count, blocks, level, pos, cache, validCoils);
                case "-" -> isBetween(blocks, level, pos, cache, validCoils);
                case "^" -> inCorner(count, blocks, level, pos, cache, validCoils);
                default -> isAtLeast(count, blocks, level, pos, cache, validCoils);
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

    private static boolean qualifies(List<Block> blocks, Level level, BlockPos pos,
                                     IMultiblockCache cache, Set<Long> validCoils) {
        BlockState bs = cache.getBlockState(level, pos);
        Block b = bs.getBlock();
        if (!blocks.contains(b)) return false;
        if (isCoil(b) && !validCoils.contains(pos.asLong())) return false;
        return true;
    }

    private static boolean isAtLeast(int n, List<Block> blocks, Level level, BlockPos pos,
                                     IMultiblockCache cache, Set<Long> validCoils) {
        int c = 0;
        for (Direction dir : Direction.values()) {
            if (qualifies(blocks, level, pos.relative(dir), cache, validCoils) && ++c >= n) return true;
        }
        return c >= n;
    }

    private static boolean isLessThan(int n, List<Block> blocks, Level level, BlockPos pos,
                                      IMultiblockCache cache, Set<Long> validCoils) {
        int c = 0;
        for (Direction dir : Direction.values()) {
            if (qualifies(blocks, level, pos.relative(dir), cache, validCoils) && ++c >= n) return false;
        }
        return c < n;
    }

    private static boolean isExact(int n, List<Block> blocks, Level level, BlockPos pos,
                                   IMultiblockCache cache, Set<Long> validCoils) {
        int c = 0;
        for (Direction dir : Direction.values()) {
            if (qualifies(blocks, level, pos.relative(dir), cache, validCoils) && ++c > n) return false;
        }
        return c == n;
    }

    private static boolean isBetween(List<Block> blocks, Level level, BlockPos pos,
                                     IMultiblockCache cache, Set<Long> validCoils) {
        for (Direction dir : Direction.values()) {
            if (qualifies(blocks, level, pos.relative(dir), cache, validCoils)
                    && qualifies(blocks, level, pos.relative(dir.getOpposite()), cache, validCoils)) {
                return true;
            }
        }
        return false;
    }

    private static boolean inCorner(int qty, List<Block> blocks, Level level, BlockPos pos,
                                    IMultiblockCache cache, Set<Long> validCoils) {
        int vertical = qualifies(blocks, level, pos.above(), cache, validCoils) ? 1 : 0;
        if (qualifies(blocks, level, pos.below(), cache, validCoils)) vertical = 1;
        int[] m = new int[4];
        for (int i = 0; i < 4; i++) {
            if (qualifies(blocks, level, pos.relative(HORIZONTAL[i]), cache, validCoils)) {
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
