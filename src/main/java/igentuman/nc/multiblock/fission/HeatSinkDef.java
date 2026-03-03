package igentuman.nc.multiblock.fission;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import igentuman.nc.NuclearCraft;
import igentuman.nc.block.fission.FissionFuelCellBlock;
import igentuman.nc.block.fission.HeatSinkBlock;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.util.TagUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.handler.config.FissionConfig.FISSION_CONFIG;
import static igentuman.nc.util.NcUtils.rlFromString;
import static igentuman.nc.util.StackUtils.getItemsByTagKey;
import static igentuman.nc.util.TagUtil.getFirstMatchingFluidByTag;

public class HeatSinkDef {

    public double heat = 0;
    public String name = "";
    public String[] rules;
    protected Validator validator;
    public final Pattern COND_FUNC = Pattern.compile("=|-|>|<|\\^");
    public final Pattern ACTIVE_CHECK = Pattern.compile("^(?!.*active_).+_heat_sink$");
    private List<FluidStack> allowedFluids;
    public static HeatSinkDef of(JsonObject asJsonObject) {
        HeatSinkDef def = new HeatSinkDef();
        try {
            def.heat = asJsonObject.get("heat").getAsDouble();
            def.name = asJsonObject.get("type").getAsString();
            JsonArray rules = asJsonObject.getAsJsonArray("placement_rule");
            String[] ruleArray = new String[rules.size()];
            for (int i = 0; i < rules.size(); i++) {
                ruleArray[i] = rules.get(i).getAsString();
            }
            def.rules = ruleArray;
            return def;
        } catch (Exception e) {
            NuclearCraft.LOGGER.error("Error parsing heatsink definition: " + e.getMessage());
            return null;
        }
    }

    public Validator getValidator() {
        if(validator == null) {
            initCondition(rules);
        }
        return validator;
    }

    private HeatSinkDef() {
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
                if (FISSION_CONFIG.ACTIVE_HEATSINK_PRIME.get() && ACTIVE_CHECK.matcher(block).matches()) {
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

    public double getHeat() {
        return heat;
    }

    public List<FluidStack> getAllowedFluids() {
        if(allowedFluids == null) {
            allowedFluids = getFluidByTagKey("forge:"+name);
        }
        return allowedFluids;
    }

    private List<FluidStack> getFluidByTagKey(String name) {
        List<FluidStack> tmp = new ArrayList<>();
        Fluid fluid = getFirstMatchingFluidByTag(name);
        tmp.add(new FluidStack(fluid,1));
        return tmp;
    }

    public static class Validator {

        private final HashMap<String[], List<String>> blockLines;
        private final HashMap<String[], List<Block>> blocks = new HashMap<>();

        public Validator(HashMap<String[], List<String>> conditions) {
            this.blockLines = conditions;
        }

        public boolean isValid(Level level, BlockPos pos, FissionReactorMultiblock multiblock)
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
        }

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
