package igentuman.nc.multiblock.fission;

import igentuman.nc.block.entity.fission.FissionBE;
import igentuman.nc.block.entity.fission.FissionHeatSinkBE;
import igentuman.nc.handler.config.CommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.handler.config.FissionConfig.HEAT_SINK_CONFIG;
import static igentuman.nc.multiblock.fission.FissionReactor.FISSION_BLOCKS;

public class HeatSinkDef {
    public double heat = 0;
    public String name = "";
    public String[] rules;

    public Validator getValidator() {
        if(validator == null) {
            rules = HEAT_SINK_CONFIG.PLACEMENT_RULES.get(name).get()
                    .toArray(new String[ HEAT_SINK_CONFIG.PLACEMENT_RULES.get(name).get().size()]);
            initCondition(rules);
        }
        return validator;
    }

    protected Validator validator;
    private boolean initialized = false;

    public HeatSinkDef() {

    }

    public HeatSinkDef(String name, int h, String...rules) {
        heat = h;
        this.name = name;
        this.rules = rules;
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

    public List<String> getItemsByTagKey(String key)
    {
        List<String> tmp = new ArrayList<>();
/*        Tag.Named<Item> tag = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(key));
        Ingredient ing = Ingredient.fromValues(Stream.of(new Ingredient.TagValue(tag)));
        for (ItemStack item: ing.getItems()) {
            tmp.add(item.getItem().toString());
        }*/
        return tmp;
    }

    public static List<Block> getBlocksByTagKey(String key)
    {
        List<Block> tmp = new ArrayList<>();
/*        Tag.Named<Block> tag = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(key));
        for(Holder<Block> holder : Registry.BLOCK.getTagOrEmpty(tag)) {
            tmp.add(holder.value());
        }*/
        return tmp;
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

    public HeatSinkDef(int i) {
        heat = i;
    }

    public HeatSinkDef config()
    {
        if(!CommonConfig.isLoaded()) {
            return this;
        }
        if(!initialized) {
            initialized = true;
            int id = FissionBlocks.heatsinks.keySet().stream().toList().indexOf(name);
            heat =  HEAT_SINK_CONFIG.HEAT.get().get(id);
        }
        return this;
    }

    public double getHeat() {
        return config().heat;
    }

    public boolean mustdDirectlyTouchFuelCell() {
        return validator.hasToTouchFuelCell();
    }

    public static class Validator {

        private FissionHeatSinkBE be;

        private HashMap<String[], List<String>> blockLines = new HashMap<>();
        private HashMap<String[], List<Block>> blocks = new HashMap<>();

        public boolean isValid(FissionHeatSinkBE be)
        {
            this.be = be;
            boolean result = false;
            for(String[] condition: blocks().keySet()) {
                switch (condition[0]) {
                    case ">":
                       result = isMoreThan(Integer.parseInt(condition[1]), blocks().get(condition));
                       break;
                    case "<":
                        result = isLessThan(Integer.parseInt(condition[1]), blocks().get(condition));
                        break;
                    case "-":
                        result = isBetween(2, blocks().get(condition));
                        break;
                    case "=":
                        result = isExact(Integer.parseInt(condition[1]), blocks().get(condition));
                        break;
                    case "^":
                        result = inCorner(Integer.parseInt(condition[1]), blocks().get(condition));
                        break;
                }
                if(!result) {
                    return false;
                }
            }
            return result;
        }

        public boolean validateFuelCellAttachment(BlockPos pos)
        {
            BlockEntity testBe = be.getLevel().getBlockEntity(pos);
            if(testBe instanceof FissionBE) {
                if(!((FissionBE)testBe).isAttachedToFuelCell()) {
                    return false;
                }
            }
            return true;
        }

        private boolean inCorner(int qty, List<Block> blocks) {
            BlockPos pos = be.getBlockPos();
            Level level = Objects.requireNonNull(be.getLevel());
            int initial = blocks.contains(level.getBlockState(pos.above(1)).getBlock()) ? 1 : 0;
            initial = blocks.contains(level.getBlockState(pos.below(1)).getBlock()) ? 1 : initial;
            int[] matches = new int[4];
            int i = 0;
            for (Direction dir: List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
                if(blocks.contains(level.getBlockState(pos.relative(dir)).getBlock())) {
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

        private boolean isExact(int s, List<Block> blocks) {
            int counter = 0;
            for (Direction dir: Direction.values()) {
                if(blocks.contains(Objects.requireNonNull(be.getLevel()).getBlockState(be.getBlockPos().relative(dir)).getBlock())) {
                    if(!validateFuelCellAttachment(be.getBlockPos().relative(dir))) {
                        continue;
                    }
                    counter++;
                    if(counter > s) return false;
                }
            }
            return counter == s;
        }

        private boolean isBetween(int s, List<Block> blocks) {
            for (Direction dir: Direction.values()) {
                if(
                        blocks.contains(Objects.requireNonNull(be.getLevel()).getBlockState(be.getBlockPos().relative(dir)).getBlock()) &&
                                blocks.contains(Objects.requireNonNull(be.getLevel()).getBlockState(be.getBlockPos().relative(dir.getOpposite())).getBlock()) &&
                                validateFuelCellAttachment(be.getBlockPos().relative(dir)) &&
                                validateFuelCellAttachment(be.getBlockPos().relative(dir.getOpposite()))
                ) {
                    return true;
                }
            }
            return false;
        }

        private boolean isLessThan(int s, List<Block> blocks) {
            int counter = 0;
            for (Direction dir: Direction.values()) {
                if(blocks.contains(Objects.requireNonNull(be.getLevel()).getBlockState(be.getBlockPos().relative(dir)).getBlock())) {
                    if(!validateFuelCellAttachment(be.getBlockPos().relative(dir))) {
                        continue;
                    }
                    counter++;
                    if(counter >= s) return false;
                }
            }
            return counter < s;
        }

        private boolean isMoreThan(int s, List<Block> blocks) {
            int counter = 0;
            for (Direction dir: Direction.values()) {
                if(blocks.contains(Objects.requireNonNull(be.getLevel()).getBlockState(be.getBlockPos().relative(dir)).getBlock())) {
                    if(!validateFuelCellAttachment(be.getBlockPos().relative(dir))) {
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
                            tmp.addAll(getBlocksByTagKey(bStr));
                        } else {
                            if (!bStr.contains(":")) {
                                bStr = MODID + ":" + bStr;
                            }
                            tmp.add(Registry.BLOCK.get(new ResourceLocation(bStr)));
                        }
                    }
                    blocks.put(condition, tmp);
                }
            }
            return blocks;
        }

        public boolean hasToTouchFuelCell() {
            for(List<Block> blockList: blocks().values()) {
                if(blockList.contains(FISSION_BLOCKS.get("fission_reactor_solid_fuel_cell").get())) {
                    return true;
                }
            }
            return false;
        }
    }
}
