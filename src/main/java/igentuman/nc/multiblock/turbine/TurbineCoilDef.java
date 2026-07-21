package igentuman.nc.multiblock.turbine;

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

public class TurbineCoilDef {

    public String name;
    public double efficiency;
    public String[] rules;
    protected Validator validator;
    public final Pattern COND_FUNC = Pattern.compile("=|-|>|<|\\^");

    public TurbineCoilDef(String name) {
        this.name = name;
        this.rules = new String[0];
    }

    public TurbineCoilDef efficiency(double efficiency) {
        this.efficiency = efficiency;
        return this;
    }

    public TurbineCoilDef rules(String... rules) {
        this.rules = rules;
        return this;
    }

    public double getEfficiency() {
        return efficiency;
    }

    public Validator getValidator() {
        if (validator == null) {
            initCondition(rules);
        }
        return validator;
    }

    private void initCondition(String[] rules) {
        HashMap<String[], List<String>> conditions = new HashMap<>();
        for (String rule : rules) {
            int cnt = 1;
            try {
                cnt = Math.max(Integer.parseInt(rule.substring(rule.length() - 1)), 1);
            } catch (NumberFormatException ignore) { }
            String[] conditionParts = rule.split("=|-|>|<|\\^");
            String[] blocks = conditionParts[0].split("\\|");
            List<String> actualBlocks = collectBlocks(blocks);
            conditions.put(new String[]{getConditionFunc(rule), String.valueOf(cnt), rule}, actualBlocks);
        }
        validator = new Validator(conditions);
    }

    private List<String> collectBlocks(String[] blocks) {
        List<String> tmp = new ArrayList<>();
        for (String block : blocks) {
            if (block.contains("#")) {
                tmp.addAll(getItemsByTagKey(block.replace("#", "")));
            } else {
                String blockName = block;
                if (!block.contains(":")) {
                    blockName = MODID + ":" + block;
                }
                tmp.add(blockName);
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
        if (!matches.isEmpty()) {
            funcType = matches.get(0);
        }
        return funcType;
    }

    public Component getPlacementRule() {
        List<String> lines = new ArrayList<>();
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
        return __("heat_sink.placement.rule", String.join(" ", lines));
    }

    public static class Validator {

        private final HashMap<String[], List<String>> blockLines;
        private final HashMap<String[], List<Block>> blocks = new HashMap<>();

        public Validator(HashMap<String[], List<String>> conditions) {
            this.blockLines = conditions;
        }

        public HashMap<String[], List<String>> blockLines() {
            return blockLines;
        }

        public HashMap<String[], List<Block>> blocks() {
            if (blocks.isEmpty()) {
                for (String[] condition : blockLines().keySet()) {
                    List<Block> tmp = new ArrayList<>();
                    for (String bStr : blockLines().get(condition)) {
                        if (bStr.contains("#")) {
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
