package igentuman.nc.multiblock.fission;

import com.google.gson.JsonArray;
import igentuman.nc.util.JSONUtil;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.*;
import static igentuman.nc.setup.registration.Tags.blockTag;
import static igentuman.nc.setup.registration.Tags.itemTag;

public class FissionBlocks {

    public static final BlockBehaviour.Properties REACTOR_BLOCKS_PROPERTIES = BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(4f).requiresCorrectToolForDrops();

    public static TagKey<Block> MODERATORS_BLOCKS = blockTag("moderators");
    public static TagKey<Block> HEAT_SINK_BLOCKS = blockTag("heat_sinks");
    public static TagKey<Block> INNER_REACTOR_BLOCKS = blockTag("reactor_inner");
    public static TagKey<Item> MODERATORS_ITEMS = itemTag("moderators");
    public static TagKey<Block> CASING_BLOCKS = blockTag("fission_reactor_casing");
    public static TagKey<Item> CASING_ITEMS = itemTag("fission_reactor_casing");


    public static final List<String> reactor =  Arrays.asList(
            "casing",
            "controller",
            "irradiation_chamber",
            "port",
            "glass",
            "solid_fuel_cell"
            //"casing_slope"
    );

    public static final HashMap<String, HeatSinkDef> heatsinks = heatsinks();
    private static HashMap<String, Double> heat;

    public static HashMap<String, HeatSinkDef> heatsinks() {
        HashMap<String, HeatSinkDef> tmp = new HashMap<>();
        List<JsonArray> data = JSONUtil.loadAllJsonFromConfig("heat_sinks");
        if(data == null) {
            return tmp;
        }
        for (JsonArray array : data) {
            for (int i = 0; i < array.size(); i++) {
                HeatSinkDef heatSink = HeatSinkDef.of(array.get(i).getAsJsonObject());
                if (heatSink != null) {
                    tmp.put(heatSink.name, heatSink);
                }
            }
        }
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
