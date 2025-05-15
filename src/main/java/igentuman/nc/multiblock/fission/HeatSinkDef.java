package igentuman.nc.multiblock.fission;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import igentuman.nc.NuclearCraft;
import igentuman.nc.block.fission.FissionFuelCellBlock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.util.TagUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_BLOCKS;
import static igentuman.nc.util.NcUtils.rlFromString;
import static igentuman.nc.util.StackUtils.getItemsByTagKey;
import static igentuman.nc.util.TagUtil.getFirstMatchingFluidByTag;

public class HeatSinkDef {

    public double heat = 0;
    public String name = "";
    public String[] rules;

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

    protected Validator validator;
    private boolean initialized = false;

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
        validator = new Validator();
        validator.blockLines = conditions;
    }

    private String getConditionFunc(String rule) {
        Pattern func = Pattern.compile("=|-|>|<|\\^");
        Matcher matcher = func.matcher(rule);
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
                if(!block.contains(":")) {
                    block = MODID+":"+block;
                }
                tmp.add(block);
            }
        }
        return tmp;
    }

    public double getHeat() {
        return heat;
    }

    public boolean mustdDirectlyTouchFuelCell() {
        return validator.hasToTouchFuelCell();
    }

    public List<FluidStack> getAllowedFluids() {
        return getFluidByTagKey("forge:"+name);
    }

    private List<FluidStack> getFluidByTagKey(String name) {
        List<FluidStack> tmp = new ArrayList<>();
        Fluid fluid = getFirstMatchingFluidByTag(name);
        tmp.add(new FluidStack(fluid,1));
        return tmp;
    }

    public static class Validator {

        private HashMap<String[], List<String>> blockLines = new HashMap<>();
        private HashMap<String[], List<Block>> blocks = new HashMap<>();

        public boolean isValid(Level level, BlockPos pos)
        {
            boolean result = false;
            BlockPos p = new BlockPos(pos);
            for(String[] condition: blocks().keySet()) {
                result = switch (condition[0]) {
                    case ">" -> isMoreThan(Integer.parseInt(condition[1]), condition, level, p);
                    case "<" -> isLessThan(Integer.parseInt(condition[1]), condition, level, p);
                    case "-" -> isBetween(condition, level, p);
                    case "=" -> isExact(Integer.parseInt(condition[1]), condition, level, p);
                    case "^" -> inCorner(Integer.parseInt(condition[1]), condition, level, p);
                    default -> result;
                };
                if(!result) {
                    return false;
                }
            }
            return result;
        }

        public boolean validateFuelCellAttachment(Level level, BlockPos...pos)
        {
            for(BlockPos p: pos) {
                for(Direction dir: Direction.values()) {
                    if(MultiblockHandler.get(level.dimension()).checkAttachmentToBlock(FissionFuelCellBlock.class, level, p, dir)) {
                        return true;
                    }
                    if(level.getBlockState(p.relative(dir)).getBlock() instanceof FissionFuelCellBlock) {
                        return true;
                    }
                }
            }

            return false;
        }

        private boolean inCorner(int qty, String[] condition, Level level, BlockPos pos) {
            int initial = blocks.get(condition).contains(level.getBlockState(pos.above(1)).getBlock()) ? 1 : 0;
            initial = blocks.get(condition).contains(level.getBlockState(pos.below(1)).getBlock()) ? 1 : initial;
            int[] matches = new int[4];
            int i = 0;
            for (Direction dir: List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
                if(blocks.get(condition).contains(level.getBlockState(pos.relative(dir)).getBlock())) {
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

        private boolean isExact(int s, String[] condition, Level level, BlockPos pos) {
            int counter = 0;
            for (Direction dir: Direction.values()) {
                if(blocks.get(condition).contains(level.getBlockState(pos.relative(dir)).getBlock())) {
                    if(mustCheckFuelCellConnection(condition) && !validateFuelCellAttachment(level, pos, pos.relative(dir) )) {
                        continue;
                    }
                    counter++;
                    if(counter > s) return false;
                }
            }
            return counter == s;
        }

        private boolean isBetween(String[] condition, Level level, BlockPos pos) {
            for (Direction dir: Direction.values()) {
                if(
                        blocks.get(condition).contains(level.getBlockState(pos.relative(dir)).getBlock()) &&
                                blocks.get(condition).contains(level.getBlockState(pos.relative(dir.getOpposite())).getBlock()) &&
                                validateFuelCellAttachment(level, pos, pos.relative(dir)) &&
                                validateFuelCellAttachment(level, pos, pos.relative(dir.getOpposite()))
                ) {
                    return true;
                }
            }
            return false;
        }

        private boolean isLessThan(int s, String[] condition, Level level, BlockPos pos) {
            int counter = 0;
            for (Direction dir: Direction.values()) {
                if(blocks.get(condition).contains(level.getBlockState(pos.relative(dir)).getBlock())) {
                    if(mustCheckFuelCellConnection(condition) && !validateFuelCellAttachment(level, pos, pos.relative(dir))) {
                        continue;
                    }
                    counter++;
                    if(counter >= s) return false;
                }
            }
            return counter < s;
        }

        private boolean mustCheckFuelCellConnection(String[] condition) {
            return !condition[2].contains("casing");
        }

        private boolean isMoreThan(int s, String[] condition, Level level, BlockPos pos) {
            int counter = 0;
            for (Direction dir: Direction.values()) {
                Block target = level.getBlockState(pos.relative(dir)).getBlock();
                if(blocks.get(condition).contains(target)) {
                    if(mustCheckFuelCellConnection(condition) && !validateFuelCellAttachment(level, pos, pos.relative(dir))) {
                        continue;
                    }
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
                            tmp.add(ForgeRegistries.BLOCKS.getValue(rlFromString(bStr)));
                        }
                    }
                    blocks.put(condition, tmp);
                }
            }
            return blocks;
        }

        public boolean hasToTouchFuelCell() {
            for(List<Block> blockList: blocks().values()) {
                 if(
                        blockList.contains(FISSION_BLOCKS.get("fission_reactor_solid_fuel_cell").get())
                        && blockList.size() == 1) {
                    return true;
                }
            }
            return false;
        }
    }
}
