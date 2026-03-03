package igentuman.nc.compat.emi;

import igentuman.nc.multiblock.accelerator.CoolerDef;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.NcUtils.rlFromString;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.convertToName;

/**
 * EMI recipe wrapper for accelerator cooler placement conditions
 * Based on the HeatSinkPlacementEmiRecipe but adapted for accelerator coolers
 */
public class CoolerPlacementEmiRecipe {
    private final ResourceLocation id;
    private final CoolerDef coolerDef;
    private final ItemStack coolerItem;
    private final List<PlacementConditionGroup> conditionGroups;

    public CoolerPlacementEmiRecipe(ResourceLocation id, CoolerDef coolerDef, ItemStack coolerItem) {
        this.id = id;
        this.coolerDef = coolerDef;
        this.coolerItem = coolerItem;
        this.conditionGroups = parseConditionGroups();
    }

    private List<PlacementConditionGroup> parseConditionGroups() {
        List<PlacementConditionGroup> groups = new ArrayList<>();
        
        if (coolerDef.getValidator() != null) {
            HashMap<String[], List<String>> blockLines = coolerDef.getValidator().blockLines();
            
            for (Map.Entry<String[], List<String>> entry : blockLines.entrySet()) {
                String[] condition = entry.getKey();
                List<String> blockNames = entry.getValue();
                
                // Convert block names to ItemStacks
                List<ItemStack> blockItems = new ArrayList<>();
                for (String blockName : blockNames) {
                    if (!blockName.contains(":")) {
                        blockName = MODID + ":" + blockName;
                    }
                    Block block = BuiltInRegistries.BLOCK.get(rlFromString(blockName));
                    if (block != null) {
                        blockItems.add(new ItemStack(block));
                    }
                }
                
                // Create condition text
                String conditionText = formatConditionText(condition, blockNames);
                
                groups.add(new PlacementConditionGroup(blockItems, conditionText));
            }
        }
        
        return groups;
    }

    private String formatConditionText(String[] condition, List<String> blockNames) {
        String operator = condition[0];
        String count = condition[1];
        
        // Get block names for display
        List<String> displayNames = new ArrayList<>();
        for (String blockName : blockNames) {
            if (!blockName.contains(":")) {
                ResourceLocation res = rl(blockName);
                Block block = BuiltInRegistries.BLOCK.get(res);
                if (block != null) {
                    displayNames.add(block.getName().getString());
                }
            } else {
                displayNames.add(convertToName(blockName.split(":")[1]));
            }
        }
        String blocksLine = String.join(" " + __("cooler.or").getString() + " ", displayNames);
        
        return switch (operator) {
            case ">" -> __("heat_sink.atleast" + (count.equals("1") ? "" : "s"), count, "").getString();
            case "<" -> __("heat_sink.less_than", count, "").getString();
            case "=" -> __("heat_sink.exact" + (count.equals("1") ? "" : "s"), count, "").getString();
            case "-" -> __("heat_sink.between", count, "").getString();
            case "^" -> __("heat_sink.in_corner", count, "").getString();
            default -> blocksLine;
        };
    }

    public ResourceLocation getId() {
        return id;
    }

    public CoolerDef getCoolerDef() {
        return coolerDef;
    }

    public ItemStack getCoolerItem() {
        return coolerItem;
    }

    public List<PlacementConditionGroup> getConditionGroups() {
        return conditionGroups;
    }

    public static class PlacementConditionGroup {
        private final List<ItemStack> requiredBlocks;
        private final String conditionText;

        public PlacementConditionGroup(List<ItemStack> requiredBlocks, String conditionText) {
            this.requiredBlocks = requiredBlocks;
            this.conditionText = conditionText;
        }

        public List<ItemStack> getRequiredBlocks() {
            return requiredBlocks;
        }

        public String getConditionText() {
            return conditionText;
        }
    }
}