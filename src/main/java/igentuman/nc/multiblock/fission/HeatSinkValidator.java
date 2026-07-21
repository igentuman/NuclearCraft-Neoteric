package igentuman.nc.multiblock.fission;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.fission.HeatSinkBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;

/** Evaluates a heat sink's neighbor placement rules against a formed fission structure to determine validity. */
public final class HeatSinkValidator {

    //i don't need this for now
    private static boolean debugLogging = false;

    private HeatSinkValidator() {}

    private static final Direction[] HORIZONTAL = {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };

    public static void debugLog(String message, Object... args) {
        if (!debugLogging) return;
        for (Object arg : args) {
            int i = message.indexOf("{}");
            if (i < 0) break;
            message = message.substring(0, i) + arg + message.substring(i + 2);
        }
        NuclearCraft.debugLog(message);
    }

    public static boolean isValid(HeatSinkDef def, Level level, BlockPos pos, FissionReactorCache fc) {
        Map<String[], List<Block>> conditions = def.getValidator().blocks();
        debugLog("[HeatSink] validate start name={} pos={} conditions={}", def.name, pos, conditions.size());
        if (conditions.isEmpty()) {
            debugLog("[HeatSink] validate OK name={} pos={} (no conditions)", def.name, pos);
            return true;
        }
        for (Map.Entry<String[], List<Block>> e : conditions.entrySet()) {
            String func = e.getKey()[0];
            int count = parseCount(e.getKey()[1]);
            String rule = e.getKey()[2];
            List<Block> blocks = e.getValue();
            boolean ok = switch (func) {
                case "<" -> isLessThan(count, blocks, level, pos, fc);
                case "=" -> isExact(count, blocks, level, pos, fc);
                case "-" -> isBetween(blocks, level, pos, fc);
                case "^" -> inCorner(count, blocks, level, pos, fc);
                default -> isAtLeast(count, blocks, level, pos, fc);
            };
            debugLog("[HeatSink] cond name={} pos={} func='{}' count={} rule='{}' blocks={} -> {}",
                    def.name, pos, func, count, rule, blocks, ok);
            if (!ok) {
                debugLog("[HeatSink] validate FAIL name={} pos={} rule='{}'", def.name, pos, rule);
                return false;
            }
        }
        debugLog("[HeatSink] validate OK name={} pos={}", def.name, pos);
        return true;
    }

    private static int parseCount(String s) {
        try {
            return Math.max(Integer.parseInt(s), 1);
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }

    private static boolean qualifies(List<Block> blocks, Level level, BlockPos pos, FissionReactorCache fc) {
        BlockState bs = fc.getBlockState(level, pos);
        Block b = bs.getBlock();
        if (!blocks.contains(b)) {
            debugLog("[HeatSink] qualify pos={} block={} -> false (not in list)", pos, b);
            return false;
        }
        long key = pos.asLong();
        if (b instanceof HeatSinkBlock && !fc.validHeatSinks.contains(key)) {
            debugLog("[HeatSink] qualify pos={} block={} -> false (heat sink not valid this pass)", pos, b);
            return false;
        }
        if (bs.is(FissionTags.MODERATORS) && !fc.activeModerators.contains(key)) {
            debugLog("[HeatSink] qualify pos={} block={} -> false (moderator inactive)", pos, b);
            return false;
        }
        debugLog("[HeatSink] qualify pos={} block={} -> true", pos, b);
        return true;
    }

    private static boolean isAtLeast(int n, List<Block> blocks, Level level, BlockPos pos, FissionReactorCache fc) {
        int c = 0;
        for (Direction dir : Direction.values()) {
            if (qualifies(blocks, level, pos.relative(dir), fc) && ++c >= n) return true;
        }
        return c >= n;
    }

    private static boolean isLessThan(int n, List<Block> blocks, Level level, BlockPos pos, FissionReactorCache fc) {
        int c = 0;
        for (Direction dir : Direction.values()) {
            if (qualifies(blocks, level, pos.relative(dir), fc) && ++c >= n) return false;
        }
        return c < n;
    }

    private static boolean isExact(int n, List<Block> blocks, Level level, BlockPos pos, FissionReactorCache fc) {
        int c = 0;
        for (Direction dir : Direction.values()) {
            if (qualifies(blocks, level, pos.relative(dir), fc) && ++c > n) return false;
        }
        return c == n;
    }

    private static boolean isBetween(List<Block> blocks, Level level, BlockPos pos, FissionReactorCache fc) {
        for (Direction dir : Direction.values()) {
            if (qualifies(blocks, level, pos.relative(dir), fc)
                    && qualifies(blocks, level, pos.relative(dir.getOpposite()), fc)) {
                return true;
            }
        }
        return false;
    }

    private static boolean inCorner(int qty, List<Block> blocks, Level level, BlockPos pos, FissionReactorCache fc) {
        int vertical = qualifies(blocks, level, pos.above(), fc) ? 1 : 0;
        if (qualifies(blocks, level, pos.below(), fc)) vertical = 1;
        int[] m = new int[4];
        for (int i = 0; i < 4; i++) {
            if (qualifies(blocks, level, pos.relative(HORIZONTAL[i]), fc)) {
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
