package igentuman.nc.multiblock.fission;

import igentuman.nc.util.TagUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rlFromString;
import static igentuman.nc.util.StackUtils.getItemsByTagKey;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.getBlockNames;

public class HeatSinkDef {

    public String name;
    public double heat;
    public String[] rules;
    protected Validator validator;
    public final Pattern COND_FUNC = Pattern.compile("=|-|>|<|\\^");
    public final Pattern ACTIVE_CHECK = Pattern.compile("^(?!.*active_).+_heat_sink$");

    public HeatSinkDef(String name) {
        this.name = name;
        this.rules = new String[0];
    }

    public HeatSinkDef heat(double heat) {
        this.heat = heat;
        return this;
    }

    public HeatSinkDef rules(String... rules) {
        this.rules = rules;
        return this;
    }

    public boolean isActive() {
        return name.startsWith("active_");
    }

    public Validator getValidator() {
        if(validator == null) {
            initCondition(rules);
        }
        return validator;
    }

    private void initCondition(String[] rules) {

        HashMap<String[], List<String>> conditions = new HashMap<>();
        for(String rule: rules) {
            int cnt = 1;
            try {
                cnt = Math.max(Integer.parseInt(rule.substring(rule.length()-1)), 1);
            } catch (NumberFormatException ignore) {  }
            String[] conditionParts = rule.split("=|-|>|<|\\^");
            String[] blocks = conditionParts[0].split("\\|");
            List<String> actualBlocks = collectBlocks(blocks);
            conditions.put(new String[] {getConditionFunc(rule), String.valueOf(cnt), rule}, actualBlocks);
        }
        validator = new Validator(conditions);
    }

    private List<String> collectBlocks(String[] blocks) {
        List<String> tmp = new ArrayList<>();
        for(String block: blocks) {
            if(block.contains("#")) {
                tmp.addAll(getItemsByTagKey(block.replace("#","")));
            } else {
                String blockName = block;
                if(!block.contains(":")) {
                    blockName = MODID + ":" + block;
                }
                tmp.add(blockName);
                if (ACTIVE_CHECK.matcher(block).matches()) {
                    if (block.contains(":")) {
                        String[] blockParts = block.split(":");
                        blockName = blockParts[0] + ":active_" + blockParts[1];
                    } else {
                        blockName = MODID + ":active_" + block;
                    }
                    tmp.add(blockName);
                }
            }
        }
        return tmp;
    }

    private String getConditionFunc(String rule) {
        Matcher matcher = COND_FUNC.matcher(rule);
        List<String> matches = new ArrayList<>();
        String funcType = ">";
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        if(!matches.isEmpty()) {
            funcType = matches.get(0);
        }
        return funcType;
    }

    public Component getPlacementRule()
    {
        List<String> lines = new ArrayList<>();
        Component placementRule;
        int i = 0;
        for (String[] condition : getValidator().blockLines().keySet()) {
            if (i > 0) {
                lines.add(__("heat_sink.and").getString());
            }
            String blocksLine = String.join(" " + __("heat_sink.or").getString() + " ", getBlockNames(condition[2]));
            switch (condition[0]) {
                case ">":
                    lines.add(__("heat_sink.atleast" + (condition[1].equals("1") ? "" : "s"), condition[1], blocksLine).getString());
                    break;
                case "-":
                    lines.add(__("heat_sink.between", condition[1], blocksLine).getString());
                    break;
                case "=":
                    lines.add(__("heat_sink.exact" + (condition[1].equals("1") ? "" : "s"), condition[1], blocksLine).getString());
                    break;
                case "<":
                    lines.add(__("heat_sink.less_than", condition[1], blocksLine).getString());
                    break;
                case "^":
                    lines.add(__("heat_sink.in_corner", condition[1], blocksLine).getString());
                    break;
            }
            i++;
        }
        placementRule = __("heat_sink.placement.rule", String.join(" ", lines));

        return placementRule;
    }

    public static class Validator {

        private final HashMap<String[], List<String>> blockLines;
        private final HashMap<String[], List<Block>> blocks = new HashMap<>();

        public Validator(HashMap<String[], List<String>> conditions) {
            this.blockLines = conditions;
        }

        /*public boolean isValid(Level level, BlockPos pos, FissionReactorMultiblock multiblock)
        {
            boolean result = false;
            BlockPos p = new BlockPos(pos);
            for(String[] condition: blocks().keySet()) {
                result = switch (condition[0]) {
                    case ">" -> isAtLeast(Integer.parseInt(condition[1]), condition, level, p, multiblock);
                    case "<" -> isLessThan(Integer.parseInt(condition[1]), condition, level, p, multiblock);
                    case "-" -> isBetween(condition, level, p, multiblock);
                    case "=" -> isExact(Integer.parseInt(condition[1]), condition, level, p, multiblock);
                    case "^" -> inCorner(Integer.parseInt(condition[1]), condition, level, p, multiblock);
                    default -> result;
                };
                if(!result) {
                    return false;
                }
            }
            return result;
        }

        public boolean validateFuelCellAttachment(Level level, AbstractMultiblock multiblock, BlockPos...pos)
        {
            for(BlockPos p: pos) {
                for(Direction dir: Direction.values()) {
                    if(multiblock != null) {
                        if(multiblock.checkAttachmentToBlock(FissionFuelCellBlock.class, level, p, dir)) {
                            return true;
                        }
                    } else if(MultiblockHandler.get(level.dimension()).checkAttachmentToBlock(FissionFuelCellBlock.class, level, p, dir)) {
                        return true;
                    }
                    BlockState block = null;
                    if(multiblock != null) {
                        block = multiblock.getBlockState(p.relative(dir));
                    }
                    if(block == null) {
                        block = level.getBlockState(p.relative(dir));
                    }
                    if(block.getBlock() instanceof FissionFuelCellBlock) {
                        return true;
                    }
                }
            }

            return false;
        }

        private BlockState getBlockState(Level level, BlockPos pos, AbstractMultiblock multiblock) {
            BlockState target = null;
            if(multiblock != null) {
                target = multiblock.getBlockState(pos);
            }
            if(target == null){
                target = level.getBlockState(pos);
            }
            return target;
        }

        private boolean validatePlacement(String[] condition, Level level, FissionReactorMultiblock multiblock, BlockPos ...pos) {
            BlockState target = getBlockState(level, pos[0], multiblock);
            if (blocks.get(condition).contains(target.getBlock())) {
                if(multiblock != null) {
                    if (target.getBlock() instanceof HeatSinkBlock && !multiblock.validHeatSinks.containsKey(pos[0].asLong())) {
                        return false;
                    }
                    if (multiblock.isModerator(target) && !multiblock.moderators.contains(pos[0].asLong())) {
                        return false;
                    }
                }
                return mustCheckFuelCellConnection(condition) || validateFuelCellAttachment(level, multiblock, pos);
            }
            return false;
        }

        private boolean inCorner(int qty, String[] condition, Level level, BlockPos pos, FissionReactorMultiblock multiblock) {
            int initial = blocks.get(condition).contains(getBlockState(level, pos.above(1), multiblock).getBlock()) ? 1 : 0;
            initial = blocks.get(condition).contains(getBlockState(level, pos.below(1), multiblock).getBlock()) ? 1 : initial;
            int[] matches = new int[4];
            int i = 0;
            for (Direction dir: List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
                if(validatePlacement(condition, level, multiblock, pos.relative(dir))) {
                    if(1+initial >= qty) return true;
                    matches[i] = 1;
                }
                i++;
            }
            for(int k = 0; k < 4; k++) {
                int next = k+1;
                if(next > 3) next = 0;
                if(matches[k] + matches[next] + initial >= qty) return true;
            }
            return false;
        }

        private boolean isExact(int s, String[] condition, Level level, BlockPos pos, FissionReactorMultiblock multiblock) {
            int counter = 0;
            for (Direction dir: Direction.values()) {
                if(validatePlacement(condition, level, multiblock, pos.relative(dir))) {
                    counter++;
                    if(counter > s) return false;
                }
            }
            return counter == s;
        }

        private boolean isBetween(String[] condition, Level level, BlockPos pos, FissionReactorMultiblock multiblock) {
            for (Direction dir: Direction.values()) {
                if(
                        validatePlacement(condition, level, multiblock, pos.relative(dir)) &&
                                validatePlacement(condition, level, multiblock, pos.relative(dir.getOpposite()))
                ) {
                    return true;
                }
            }
            return false;
        }

        private boolean isLessThan(int s, String[] condition, Level level, BlockPos pos, FissionReactorMultiblock multiblock) {
            int counter = 0;
            for (Direction dir: Direction.values()) {
                if(validatePlacement(condition, level, multiblock, pos.relative(dir))) {
                    counter++;
                    if(counter >= s) return false;
                }
            }
            return counter < s;
        }

        private boolean mustCheckFuelCellConnection(String[] condition) {
            return condition[2].contains("casing");
        }

        private boolean isAtLeast(int s, String[] condition, Level level, BlockPos pos, FissionReactorMultiblock multiblock) {
            int counter = 0;
            for (Direction dir: Direction.values()) {
                if(validatePlacement(condition, level, multiblock, pos.relative(dir))) {
                    counter++;
                    if(counter >= s) return true;
                }
            }
            return counter >= s;
        }*/

        public HashMap<String[], List<String>> blockLines()
        {
            return blockLines;
        }

        public HashMap<String[], List<Block>> blocks()
        {
            if(blocks.isEmpty()) {
                for (String[] condition: blockLines().keySet()) {
                    List<Block> tmp = new ArrayList<>();
                    for(String bStr: blockLines().get(condition)) {
                        if(bStr.contains("#")) {
                            tmp.addAll(TagUtil.getBlocksByTagKey(bStr));
                        } else {
                            if (!bStr.contains(":")) {
                                bStr = MODID + ":" + bStr;
                            }
                            tmp.add(BuiltInRegistries.BLOCK.get(rlFromString(bStr)));
                        }
                    }
                    blocks.put(condition, tmp);
                }
            }
            return blocks;
        }
    }
}
