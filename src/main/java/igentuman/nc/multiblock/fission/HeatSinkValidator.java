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

/**
 * Evaluates a {@link HeatSinkDef}'s placement rules against a formed structure, reading neighbor
 * states from the {@link FissionReactorCache}. Rules are parsed by {@link HeatSinkDef} into
 * {@code [function, count, ruleText] -> blocks} conditions; all conditions must pass (AND).
 *
 * <p>A neighbor counts toward a condition only if its block is in the condition's block list and:
 * a neighboring heat sink must already be valid this pass (hence the topological
 * {@code HS_SCHEDULE}); a neighboring moderator must be active (face-adjacent to a fuel cell).
 *
 * <p>Deviation from legacy: the legacy "neighbor must also be adjacent to a fuel cell unless the
 * rule mentions casing" gate is dropped. It invalidated otherwise-correct basic sinks and was
 * keyed on a literal substring of the rule text; the operator counts already express the intent.
 */
public final class HeatSinkValidator {

    private HeatSinkValidator() {}

    private static final Direction[] HORIZONTAL = {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };

    public static boolean isValid(HeatSinkDef def, Level level, BlockPos pos, FissionReactorCache fc) {
        Map<String[], List<Block>> conditions = def.getValidator().blocks();
        NuclearCraft.LOGGER.debug("[HeatSink] validate start name={} pos={} conditions={}", def.name, pos, conditions.size());
        if (conditions.isEmpty()) {
            NuclearCraft.LOGGER.debug("[HeatSink] validate OK name={} pos={} (no conditions)", def.name, pos);
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
            NuclearCraft.LOGGER.debug("[HeatSink] cond name={} pos={} func='{}' count={} rule='{}' blocks={} -> {}",
                    def.name, pos, func, count, rule, blocks, ok);
            if (!ok) {
                NuclearCraft.LOGGER.debug("[HeatSink] validate FAIL name={} pos={} rule='{}'", def.name, pos, rule);
                return false;
            }
        }
        NuclearCraft.LOGGER.debug("[HeatSink] validate OK name={} pos={}", def.name, pos);
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
            NuclearCraft.LOGGER.debug("[HeatSink] qualify pos={} block={} -> false (not in list)", pos, b);
            return false;
        }
        long key = pos.asLong();
        if (b instanceof HeatSinkBlock && !fc.validHeatSinks.contains(key)) {
            NuclearCraft.LOGGER.debug("[HeatSink] qualify pos={} block={} -> false (heat sink not valid this pass)", pos, b);
            return false;
        }
        if (bs.is(FissionTags.MODERATORS) && !fc.activeModerators.contains(key)) {
            NuclearCraft.LOGGER.debug("[HeatSink] qualify pos={} block={} -> false (moderator inactive)", pos, b);
            return false;
        }
        NuclearCraft.LOGGER.debug("[HeatSink] qualify pos={} block={} -> true", pos, b);
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
