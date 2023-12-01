package igentuman.nc.multiblock.fission;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

import java.util.*;

import static igentuman.nc.NuclearCraft.MODID;

public class FissionBlocks {
    public static final BlockBehaviour.Properties REACTOR_BLOCKS_PROPERTIES = BlockBehaviour.Properties.of(Material.METAL).strength(4f).requiresCorrectToolForDrops();
    public static TagKey<Block> MODERATORS_BLOCKS = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MODID, "moderators"));
    public static TagKey<Block> HEAT_SINK_BLOCKS = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MODID, "heat_sinks"));
    public static TagKey<Block> INNER_REACTOR_BLOCKS = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MODID, "reactor_inner"));
    public static TagKey<Item> MODERATORS_ITEMS = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(MODID, "moderators"));
    public static TagKey<Block> CASING_BLOCKS = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MODID, "fission_reactor_casing"));
    public static TagKey<Item> CASING_ITEMS = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(MODID, "fission_reactor_casing"));

    public static final List<String> reactor =  Arrays.asList(
            "casing",
            "controller",
            "irradiation_chamber",
            "port",
            "glass",
            "solid_fuel_cell"
    );

    public static final HashMap<String, HeatSinkDef> heatsinks = heatsinks();
    private static HashMap<String, Double> heat;

    public static HashMap<String, HeatSinkDef> heatsinks() {
        HashMap<String, HeatSinkDef> tmp = new HashMap<>();
        tmp.put("empty_active", new HeatSinkDef());
        tmp.put("empty", new HeatSinkDef());
        tmp.put("aluminum",  new HeatSinkDef("aluminum", 175, "quartz_heat_sink","lapis_heat_sink"));
        tmp.put("arsenic", new HeatSinkDef("arsenic", 135, "#nuclearcraft:moderators>3"));
        tmp.put("boron", new HeatSinkDef("boron", 160, "quartz_heat_sink","#nuclearcraft:fission_reactor_casing|#nuclearcraft:moderators"));
        tmp.put("carobbiite", new HeatSinkDef("carobbiite", 140, "end_stone_heat_sink","copper_heat_sink"));
        tmp.put("copper", new HeatSinkDef("copper", 80, "glowstone_heat_sink"));
        tmp.put("cryotheum", new HeatSinkDef("cryotheum", 160, "fission_reactor_solid_fuel_cell>2"));
        tmp.put("diamond", new HeatSinkDef("diamond", 150, "copper_heat_sink","quartz_heat_sink"));
        tmp.put("emerald", new HeatSinkDef("emerald", 160, "fission_reactor_solid_fuel_cell","#nuclearcraft:moderators"));
        tmp.put("end_stone", new HeatSinkDef("end_stone", 40,  "enderium_heat_sink"));
        tmp.put("enderium", new HeatSinkDef("enderium", 120, "#nuclearcraft:fission_reactor_casing^3"));
        tmp.put("fluorite", new HeatSinkDef("fluorite", 160, "gold_heat_sink","prismarine_heat_sink"));
        tmp.put("glowstone", new HeatSinkDef("glowstone", 130, "#nuclearcraft:moderators>2"));
        tmp.put("gold", new HeatSinkDef("gold", 120, "water_heat_sink", "redstone_heat_sink"));
        tmp.put("iron", new HeatSinkDef("iron", 80, "gold_heat_sink"));
        tmp.put("lapis", new HeatSinkDef("lapis", 120, "fission_reactor_solid_fuel_cell", "#nuclearcraft:fission_reactor_casing"));
        tmp.put("lead", new HeatSinkDef("lead", 60, "iron_heat_sink"));
        tmp.put("liquid_helium", new HeatSinkDef("liquid_helium", 140, "redstone_heat_sink", "#nuclearcraft:fission_reactor_casing"));
        tmp.put("liquid_nitrogen", new HeatSinkDef("liquid_nitrogen", 185, "copper_heat_sink","purpur_heat_sink"));
        tmp.put("lithium", new HeatSinkDef("lithium", 130, "lead_heat_sink-2"));
        tmp.put("magnesium", new HeatSinkDef("magnesium", 110, "#nuclearcraft:fission_reactor_casing","#nuclearcraft:moderators"));
        tmp.put("manganese", new HeatSinkDef("manganese", 150, "fission_reactor_solid_fuel_cell>2"));
        tmp.put("nether_brick", new HeatSinkDef("nether_brick", 70,  "obsidian_heat_sink"));
        tmp.put("obsidian", new HeatSinkDef("obsidian", 40, "glowstone_heat_sink-2"));
        tmp.put("prismarine", new HeatSinkDef("prismarine", 115, "water_heat_sink"));
        tmp.put("purpur", new HeatSinkDef("purpur", 95, "#nuclearcraft:fission_reactor_casing","iron_heat_sink"));
        tmp.put("quartz", new HeatSinkDef("quartz", 90, "#nuclearcraft:moderators"));
        tmp.put("redstone", new HeatSinkDef("redstone", 90, "fission_reactor_solid_fuel_cell"));
        tmp.put("silver", new HeatSinkDef("silver", 170, "glowstone_heat_sink>2","tin_heat_sink"));
        tmp.put("slime", new HeatSinkDef("slime", 145, "water_heat_sink","lead_heat_sink"));
        tmp.put("tin", new HeatSinkDef("tin", 120,  "lapis_heat_sink-2"));
        tmp.put("villiaumite", new HeatSinkDef("villiaumite", 180, "redstone_heat_sink","end_stone_heat_sink"));
        tmp.put("water", new HeatSinkDef("water", 60,"fission_reactor_solid_fuel_cell|#nuclearcraft:moderators"));
        return tmp;
    }

    public static HashMap<String, Double> initialHeat()
    {
        if(heat == null) {
            heat = new HashMap<>();
            for(String name: heatsinks().keySet()) {
                if(name.contains("empty")) continue;
                    heat.put(name, heatsinks().get(name).heat);
            }
        }

        return heat;
    }

    public static List<String> initialPlacementRules(String name) {
        return List.of(heatsinks().get(name).rules);
    }
}
