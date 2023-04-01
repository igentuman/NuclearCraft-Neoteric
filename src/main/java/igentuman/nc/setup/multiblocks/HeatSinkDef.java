package igentuman.nc.setup.multiblocks;

import igentuman.nc.handler.config.CommonConfig;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class HeatSinkDef {
    public double heat = 0;
    public String name = "";
    public String[] rules;

    public Validator getValidator() {
        if(validator == null) {
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
            String[] conditionParts = rule.split("=|-|>|<");
            String[] blocks = conditionParts[0].split("\\|");
            List<String> actualBlocks = collectBlocks(blocks);
            conditions.put(new String[] {getConditionFunc(rule), String.valueOf(cnt), rule}, actualBlocks);
        }
        validator = new Validator();
        validator.blockLines = conditions;
    }

    private String getConditionFunc(String rule) {
        Pattern func = Pattern.compile("=|-|>|<");
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

    private List<String> getItemsByTagKey(String key)
    {
        List<String> tmp = new ArrayList<>();
        TagKey<Item> tag = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(key));
        Ingredient ing = Ingredient.fromValues(Stream.of(new Ingredient.TagValue(tag)));
        for (ItemStack item: ing.getItems()) {
            tmp.add(item.getItem().toString());
        }
        return tmp;
    }

    private List<String> collectBlocks(String[] blocks) {
        List<String> tmp = new ArrayList<>();
        for(String block: blocks) {
            if(block.contains(":")) {
                tmp.addAll(getItemsByTagKey(block));
            } else {
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
            heat = CommonConfig.HeatSinkConfig.HEAT.get().get(id);
        }
        return this;
    }

    public double getHeat() {
        return config().heat;
    }

    public static class Validator {

        private HashMap<String[], List<String>> blockLines = new HashMap<>();
        private HashMap<String[], List<Block>> blocks = new HashMap<>();

        public boolean isValid()
        {
            return true;
        }

        public HashMap<String[], List<String>> blockLines()
        {
            return blockLines;
        }

        public HashMap<String[], List<Block>> blocks()
        {
            if(blocks.isEmpty()) {
                for (String[] condition: blockLines().keySet()) {

                }
            }
            return blocks;
        }

    }

}
