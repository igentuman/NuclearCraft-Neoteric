package igentuman.nc.client.gui.fission.designer;

import igentuman.nc.block.fission.HeatSinkBlock;
import igentuman.nc.multiblock.fission.HeatSinkDef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Set;

public class HeatsinkRuleEvaluator {

    private final DesignGrid grid;
    private final Set<BlockPos> validHeatSinks;
    private final Set<BlockPos> moderators;
    private final Set<BlockPos> directFuelCellConnection;
    private final Set<BlockPos> secondFuelCellConnection;
    private final Block casing;

    private HeatSinkDef.Validator currentValidator;

    public HeatsinkRuleEvaluator(DesignGrid grid,
                                 Set<BlockPos> validHeatSinks,
                                 Set<BlockPos> moderators,
                                 Set<BlockPos> directFuelCellConnection,
                                 Set<BlockPos> secondFuelCellConnection) {
        this.grid = grid;
        this.validHeatSinks = validHeatSinks;
        this.moderators = moderators;
        this.directFuelCellConnection = directFuelCellConnection;
        this.secondFuelCellConnection = secondFuelCellConnection;
        this.casing = DesignBlocks.casing();
    }

    public boolean evaluate(HeatSinkDef def, BlockPos pos) {
        HeatSinkDef.Validator validator = def.getValidator();
        if (validator == null) {
            return false;
        }
        currentValidator = validator;
        boolean result = false;
        for (String[] condition : validator.blocks().keySet()) {
            result = switch (condition[0]) {
                case ">" -> isAtLeast(Integer.parseInt(condition[1]), condition, pos);
                case "<" -> isLessThan(Integer.parseInt(condition[1]), condition, pos);
                case "-" -> isBetween(condition, pos);
                case "=" -> isExact(Integer.parseInt(condition[1]), condition, pos);
                case "^" -> inCorner(Integer.parseInt(condition[1]), condition, pos);
                default -> result;
            };
            if (!result) {
                return false;
            }
        }
        return result;
    }

    private Block blockAt(BlockPos pos) {
        if (!grid.inBounds(pos.getX(), pos.getY(), pos.getZ())) {
            return casing;
        }
        return grid.cells.get(pos);
    }

    private boolean matchesBlockList(String[] condition, BlockPos pos) {
        Block target = blockAt(pos);
        return target != null && currentValidator.blocks().get(condition).contains(target);
    }

    private boolean validatePlacement(String[] condition, BlockPos pos) {
        Block target = blockAt(pos);
        if (target == null || !currentValidator.blocks().get(condition).contains(target)) {
            return false;
        }
        if (target instanceof HeatSinkBlock && !validHeatSinks.contains(pos)) {
            return false;
        }
        if (DesignBlocks.isModerator(target) && !moderators.contains(pos)) {
            return false;
        }
        return mustCheckFuelCellConnection(condition) || validateFuelCellAttachment(pos);
    }

    private boolean mustCheckFuelCellConnection(String[] condition) {
        return condition[2].contains("casing");
    }

    private boolean validateFuelCellAttachment(BlockPos p) {
        for (Direction dir : Direction.values()) {
            BlockPos np = p.relative(dir);
            if (directFuelCellConnection.contains(np)) {
                return true;
            }
            if (secondFuelCellConnection.contains(np) && blockAt(np) != null) {
                return true;
            }
            if (DesignBlocks.isFuelCell(blockAt(np))) {
                return true;
            }
        }
        return false;
    }

    private boolean isAtLeast(int s, String[] condition, BlockPos pos) {
        int counter = 0;
        for (Direction dir : Direction.values()) {
            if (validatePlacement(condition, pos.relative(dir))) {
                counter++;
                if (counter >= s) {
                    return true;
                }
            }
        }
        return counter >= s;
    }

    private boolean isLessThan(int s, String[] condition, BlockPos pos) {
        int counter = 0;
        for (Direction dir : Direction.values()) {
            if (validatePlacement(condition, pos.relative(dir))) {
                counter++;
                if (counter >= s) {
                    return false;
                }
            }
        }
        return counter < s;
    }

    private boolean isExact(int s, String[] condition, BlockPos pos) {
        int counter = 0;
        for (Direction dir : Direction.values()) {
            if (validatePlacement(condition, pos.relative(dir))) {
                counter++;
                if (counter > s) {
                    return false;
                }
            }
        }
        return counter == s;
    }

    private boolean isBetween(String[] condition, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (validatePlacement(condition, pos.relative(dir))
                    && validatePlacement(condition, pos.relative(dir.getOpposite()))) {
                return true;
            }
        }
        return false;
    }

    private boolean inCorner(int qty, String[] condition, BlockPos pos) {
        int initial = matchesBlockList(condition, pos.above(1)) ? 1 : 0;
        initial = matchesBlockList(condition, pos.below(1)) ? 1 : initial;
        int[] matches = new int[4];
        int i = 0;
        for (Direction dir : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
            if (validatePlacement(condition, pos.relative(dir))) {
                if (1 + initial >= qty) {
                    return true;
                }
                matches[i] = 1;
            }
            i++;
        }
        for (int k = 0; k < 4; k++) {
            int next = k + 1 > 3 ? 0 : k + 1;
            if (matches[k] + matches[next] + initial >= qty) {
                return true;
            }
        }
        return false;
    }
}
