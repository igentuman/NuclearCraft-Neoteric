package igentuman.nc.multiblock.accelerator;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static igentuman.nc.NuclearCraft.rl;

public record CoolerDef(ResourceLocation id, long coolingPerTick, List<String> placementRules) {

    private static final Map<String, CoolerDef> BY_NAME = new HashMap<>();

    static {
        register("aluminum_cooler", 175, "quartz_cooler", "tin_cooler");
        register("arsenic_cooler", 145, "#nuclearcraft:rf_amplifiers>2");
        register("boron_cooler", 105, "electromagnet_yoke", "#nuclearcraft:rf_amplifiers");
        register("carobbiite_cooler", 140, "end_stone_cooler", "gold_cooler");
        register("copper_cooler", 80, "water_cooler");
        register("cryotheum_cooler", 205, "tin_cooler>3");
        register("diamond_cooler", 185, "gold_cooler", "prismarine_cooler");
        register("emerald_cooler", 135, "#nuclearcraft:rf_amplifiers", "prismarine_cooler");
        register("empty_cooler", 0);
        register("end_stone_cooler", 50, "electromagnet_yoke");
        register("enderium_cooler", 190, "purpur_cooler>3");
        register("fluorite_cooler", 155, "gold_cooler>3");
        register("glowstone_cooler", 110, "#nuclearcraft:electromagnets>2");
        register("gold_cooler", 95, "iron_cooler>2");
        register("iron_cooler", 55, "#nuclearcraft:electromagnets");
        register("lapis_cooler", 130, "electromagnet_yoke", "#nuclearcraft:electromagnets");
        register("lead_cooler", 65, "iron_cooler");
        register("liquid_helium_cooler", 200, "boron_cooler", "lapis_cooler");
        register("liquid_nitrogen_cooler", 195, "lapis_cooler", "gold_cooler");
        register("lithium_cooler", 125, "boron_cooler");
        register("magnesium_cooler", 150, "end_stone_cooler", "prismarine_cooler");
        register("manganese_cooler", 180, "gold_cooler", "quartz_cooler");
        register("nether_brick_cooler", 90, "obsidian_cooler");
        register("obsidian_cooler", 70, "glowstone_cooler>2");
        register("prismarine_cooler", 85, "water_cooler>2");
        register("purpur_cooler", 100, "end_stone_cooler>2");
        register("quartz_cooler", 75, "redstone_cooler");
        register("redstone_cooler", 115, "#nuclearcraft:rf_amplifiers", "#nuclearcraft:electromagnets");
        register("silver_cooler", 160, "arsenic_cooler>2");
        register("slime_cooler", 165, "lead_cooler>2", "water_cooler");
        register("tin_cooler", 120, "lapis_cooler>2");
        register("villiaumite_cooler", 170, "purpur_cooler", "prismarine_cooler");
        register("water_cooler", 60, "#nuclearcraft:rf_amplifiers");
    }

    private static void register(String name, long coolingPerTick, String... placementRules) {
        BY_NAME.put(name, new CoolerDef(rl(name), coolingPerTick, List.of(placementRules)));
    }

    public static CoolerDef get(String blockName) {
        return BY_NAME.get(blockName);
    }

    public boolean satisfiesPlacementRules(List<BlockState> neighbors) {
        for (String rule : placementRules) {
            String[] parts = rule.split(">", 2);
            int required = parts.length == 2 ? Integer.parseInt(parts[1]) : 1;
            boolean tag = parts[0].startsWith("#");
            String name = tag ? parts[0].substring(1) : parts[0];
            ResourceLocation target = name.contains(":") ? ResourceLocation.parse(name) : rl(name);
            long matches = neighbors.stream().filter(state -> tag
                    ? state.is(TagKey.create(Registries.BLOCK, target))
                    : BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(target)).count();
            if (matches < required) return false;
        }
        return true;
    }
}
