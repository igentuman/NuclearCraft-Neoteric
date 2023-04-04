package igentuman.nc.setup.multiblocks;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;

public class FissionBlocks {
    public static final BlockBehaviour.Properties REACTOR_BLOCKS_PROPERTIES = BlockBehaviour.Properties.of(Material.METAL).strength(2f).requiresCorrectToolForDrops();
    public static TagKey<Block> MODERATORS_BLOCKS = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MODID, "moderators"));
    public static TagKey<Item> MODERATORS_ITEMS = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(MODID, "moderators"));

    public static TagKey<Block> CASING_BLOCKS = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MODID, "fission_reactor_casing"));
    public static TagKey<Item> CASING_ITEMS = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(MODID, "fission_reactor_casing"));

    public static final List<String> reactor =  Arrays.asList(
            "casing",
            "controller",
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
        tmp.put("aluminum",  new HeatSinkDef("aluminum", 0, "fission_reactor_solid_fuel_cell>1"));
        tmp.put("arsenic", new HeatSinkDef("arsenic", 0, "fission_reactor_solid_fuel_cell"));
        tmp.put("boron", new HeatSinkDef("boron", 0, "fission_reactor_solid_fuel_cell"));
        tmp.put("carobbiite", new HeatSinkDef("carobbiite", 0, "fission_reactor_solid_fuel_cell"));
        tmp.put("copper", new HeatSinkDef("copper", 80, "glowstone_heat_sink"));
        tmp.put("cryotheum", new HeatSinkDef("cryotheum", 160, "fission_reactor_solid_fuel_cell>2"));
        tmp.put("diamond", new HeatSinkDef("diamond", 150, "copper_heat_sink","quartz_heat_sink"));
        tmp.put("emerald", new HeatSinkDef("emerald", 160, "fission_reactor_solid_fuel_cell","nuclearcraft:moderators"));
        tmp.put("end_stone", new HeatSinkDef("end_stone", 0,  "fission_reactor_solid_fuel_cell"));
        tmp.put("enderium", new HeatSinkDef("enderium", 120, "nuclearcraft:fission_reactor_casing=3"));
        tmp.put("fluorite", new HeatSinkDef("fluorite", 0, "fission_reactor_solid_fuel_cell"));
        tmp.put("glowstone", new HeatSinkDef("glowstone", 0, "nuclearcraft:moderators>2"));
        tmp.put("gold", new HeatSinkDef("gold", 120, "water_heat_sink", "redstone_heat_sink"));
        tmp.put("iron", new HeatSinkDef("iron", 80, "gold_heat_sink"));
        tmp.put("lapis", new HeatSinkDef("lapis", 120, "fission_reactor_solid_fuel_cell", "nuclearcraft:fission_reactor_casing"));
        tmp.put("lead", new HeatSinkDef("lead", 0, "fission_reactor_solid_fuel_cell"));
        tmp.put("liquid_helium", new HeatSinkDef("liquid_helium", 140, "redstone_heat_sink", "nuclearcraft:fission_reactor_casing"));
        tmp.put("liquid_nitrogen", new HeatSinkDef("liquid_nitrogen", 0, "fission_reactor_solid_fuel_cell"));
        tmp.put("lithium", new HeatSinkDef("lithium", 0, "fission_reactor_solid_fuel_cell"));
        tmp.put("magnesium", new HeatSinkDef("magnesium", 110, "nuclearcraft:fission_reactor_casing","nuclearcraft:moderators"));
        tmp.put("manganese", new HeatSinkDef("manganese", 0, "fission_reactor_solid_fuel_cell"));
        tmp.put("nether_brick", new HeatSinkDef("nether_brick", 0,  "fission_reactor_solid_fuel_cell"));
        tmp.put("obsidian", new HeatSinkDef("obsidian", 0, "fission_reactor_solid_fuel_cell"));
        tmp.put("prismarine", new HeatSinkDef("prismarine", 0, "fission_reactor_solid_fuel_cell"));
        tmp.put("purpur", new HeatSinkDef("purpur", 0, "fission_reactor_solid_fuel_cell"));
        tmp.put("quartz", new HeatSinkDef("quartz", 90, "nuclearcraft:moderators"));
        tmp.put("redstone", new HeatSinkDef("redstone", 90, "fission_reactor_solid_fuel_cell"));
        tmp.put("silver", new HeatSinkDef("silver", 0, "fission_reactor_solid_fuel_cell"));
        tmp.put("slime", new HeatSinkDef("slime", 0, "fission_reactor_solid_fuel_cell"));
        tmp.put("tin", new HeatSinkDef("tin", 120,  "lapis_heat_sink-2"));
        tmp.put("villiaumite", new HeatSinkDef("villiaumite", 90));
        tmp.put("water", new HeatSinkDef("water", 60,"fission_reactor_solid_fuel_cell|nuclearcraft:moderators"));
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
}
