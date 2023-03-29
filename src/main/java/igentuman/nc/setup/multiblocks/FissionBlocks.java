package igentuman.nc.setup.multiblocks;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class FissionBlocks {
    public static final BlockBehaviour.Properties REACTOR_BLOCKS_PROPERTIES = BlockBehaviour.Properties.of(Material.METAL).strength(2f).requiresCorrectToolForDrops();

    public static final List<String> reactor =  Arrays.asList(
            "casing",
            "controller",
            "buffer",
            "port",
            "glass",
            "control_port",
            "solid_fuel_cell"
    );

    public static final HashMap<String, HeatSinkDef> heatsinks = heatsinks();

    private static HashMap<String, HeatSinkDef> heatsinks() {
        HashMap<String, HeatSinkDef> tmp = new HashMap<>();
        tmp.put("empty_active", new HeatSinkDef());
        tmp.put("empty", new HeatSinkDef());
        tmp.put("aluminum",  new HeatSinkDef());
        tmp.put("arsenic", new HeatSinkDef());
        tmp.put("boron", new HeatSinkDef());
        tmp.put("carobbiite", new HeatSinkDef());
        tmp.put("copper", new HeatSinkDef());
        tmp.put("cryotheum", new HeatSinkDef());
        tmp.put("diamond", new HeatSinkDef());
        tmp.put("emerald", new HeatSinkDef());
        tmp.put("end_stone", new HeatSinkDef());
        tmp.put("enderium", new HeatSinkDef());
        tmp.put("fluorite", new HeatSinkDef());
        tmp.put("glowstone", new HeatSinkDef());
        tmp.put("gold", new HeatSinkDef());
        tmp.put("iron", new HeatSinkDef());
        tmp.put("lapis", new HeatSinkDef());
        tmp.put("lead", new HeatSinkDef());
        tmp.put("liquid_helium", new HeatSinkDef());
        tmp.put("liquid_nitrogen", new HeatSinkDef());
        tmp.put("lithium", new HeatSinkDef());
        tmp.put("magnesium", new HeatSinkDef());
        tmp.put("manganese", new HeatSinkDef());
        tmp.put("nether_brick", new HeatSinkDef());
        tmp.put("obsidian", new HeatSinkDef());
        tmp.put("prismarine", new HeatSinkDef());
        tmp.put("purpur", new HeatSinkDef());
        tmp.put("quartz", new HeatSinkDef());
        tmp.put("redstone", new HeatSinkDef());
        tmp.put("silver", new HeatSinkDef());
        tmp.put("slime", new HeatSinkDef());
        tmp.put("tin", new HeatSinkDef());
        tmp.put("villiaumite", new HeatSinkDef());
        tmp.put("water", new HeatSinkDef());
        return tmp;
    }
}
