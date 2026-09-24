package igentuman.nc.compat;

import igentuman.nc.NuclearCraft;
import igentuman.nc.api.multiblock.part.CoolerDef;
import igentuman.nc.registration.HeatSinkEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record PlacementRuleRecipe(ResourceLocation id, ItemStack part, long cooling, List<Condition> conditions) {

    public record Condition(Component description, List<ItemStack> alternatives) {}

    public static List<PlacementRuleRecipe> heatSinks() {
        List<PlacementRuleRecipe> recipes = new ArrayList<>();
        for (HeatSinkEntry entry : ModEntries.HEAT_SINKS.values()) {
            if (!entry.isEnabled() || entry.def().heat <= 0) continue;
            List<Condition> conditions = new ArrayList<>();
            for (var line : entry.def().getValidator().blockLines().entrySet()) {
                String[] rule = line.getKey();
                String key = switch (rule[0]) {
                    case "=" -> "heat_sink.exact" + (rule[1].equals("1") ? "" : "s");
                    case "<" -> "heat_sink.less_than";
                    case "-" -> "heat_sink.between";
                    case "^" -> "heat_sink.in_corner";
                    default -> "heat_sink.atleast" + (rule[1].equals("1") ? "" : "s");
                };
                List<ItemStack> blocks = line.getValue().stream().map(PlacementRuleRecipe::blockStack)
                        .filter(stack -> !stack.isEmpty()).distinct().toList();
                conditions.add(new Condition(Component.translatable(key, rule[1], ""), blocks));
            }
            conditions.sort(Comparator.comparing(condition -> condition.description().getString()));
            recipes.add(new PlacementRuleRecipe(NuclearCraft.rl("heat_sink_placement/" + entry.name),
                    new ItemStack(entry.block().get()), (long) entry.def().heat, conditions));
        }
        return recipes;
    }

    public static List<PlacementRuleRecipe> coolers() {
        List<PlacementRuleRecipe> recipes = new ArrayList<>();
        for (String name : igentuman.nc.setup.entries.Accelerator.COOLERS) {
            CoolerDef def = CoolerDef.get(name);
            var entry = ModEntries.get(name);
            if (def == null || entry == null || !entry.hasItem() || !entry.isEnabled() || def.coolingPerTick() <= 0) continue;
            List<Condition> conditions = new ArrayList<>();
            for (String rule : def.placementRules()) {
                String[] parts = rule.split(">", 2);
                int count = parts.length == 2 ? Integer.parseInt(parts[1]) : 1;
                List<ItemStack> blocks;
                if (parts[0].startsWith("#")) {
                    var tag = TagKey.create(Registries.BLOCK, ResourceLocation.parse(parts[0].substring(1)));
                    blocks = new ArrayList<>();
                    BuiltInRegistries.BLOCK.getTagOrEmpty(tag).forEach(holder -> {
                        ItemStack stack = new ItemStack(holder.value());
                        if (!stack.isEmpty()) blocks.add(stack);
                    });
                } else {
                    ItemStack stack = blockStack(parts[0]);
                    blocks = stack.isEmpty() ? List.of() : List.of(stack);
                }
                conditions.add(new Condition(Component.translatable(count == 1 ? "heat_sink.atleast" : "heat_sink.atleasts", count, ""), blocks));
            }
            recipes.add(new PlacementRuleRecipe(NuclearCraft.rl("cooler_placement/" + name),
                    new ItemStack(entry.item().get()), def.coolingPerTick(), conditions));
        }
        return recipes;
    }

    private static ItemStack blockStack(String name) {
        ResourceLocation id = name.contains(":") ? ResourceLocation.parse(name) : NuclearCraft.rl(name);
        Block block = BuiltInRegistries.BLOCK.get(id);
        return block == Blocks.AIR ? ItemStack.EMPTY : new ItemStack(block);
    }
}
