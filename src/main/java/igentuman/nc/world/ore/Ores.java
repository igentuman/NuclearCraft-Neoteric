package igentuman.nc.world.ore;

import igentuman.nc.handler.config.CommonConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.world.dimension.Dimensions.WASTELAIND_ID;

public class Ores {

    private static HashMap<String, NCOre> all;
    private static HashMap<String, NCOre> registered;

    public static HashMap<String, NCOre> registered()
    {
        if(registered == null) {
            registered = new HashMap<>();
            for(String name: all().keySet()) {
                if(all().get(name).isRegistered()) {
                    registered.put(name, all().get(name));
                }
            }
        }
        return registered;
    }

    public static HashMap<String, NCOre> all()
    {
        if(all == null) {
            int wasteland = WASTELAIND_ID;
            all = new HashMap<>();
            all.put("uranium", NCOre.get("uranium").vein(7, 3).height(-10, 40).dim(0, wasteland));
            all.put("thorium", NCOre.get("thorium").vein(7, 3).height(-10, 40).dim(0, wasteland));
            all.put("boron", NCOre.get("boron").vein(7, 3).height(-10, 40).dim(0, wasteland));
            all.put("lead", NCOre.get("lead").vein(7, 3).height(-10, 40).dim(0, wasteland));
            all.put("tin", NCOre.get("tin").vein(7, 3).height(-10, 40).dim(0, wasteland));
            all.put("zinc", NCOre.get("zinc").vein(7, 3).height(-10, 40).dim(0, wasteland));
            all.put("magnesium", NCOre.get("magnesium").vein(7, 3).height(-10, 40).dim(0, wasteland));
            all.put("lithium", NCOre.get("lithium").vein(7, 3).height(-10, 40).dim(0, wasteland));
            all.put("cobalt", NCOre.get("cobalt").vein(7, 3).height(-10, 40).dim(0, wasteland));
            all.put("platinum", NCOre.get("platinum").vein(7, 3).height(-10, 40).dim(0, wasteland));
        }
        return all;
    }

    public static List<Boolean> initialOreRegistration()
    {
        List<Boolean> tmp = new ArrayList<>();
        for(NCOre ore: all().values()) {
            tmp.add(true);
        }
        return tmp;
    }

    public static List<Integer> initialOreVeinSizes()
    {
        List<Integer> tmp = new ArrayList<>();
        for(NCOre ore: all().values()) {
            tmp.add(ore.veinSize);
        }
        return tmp;
    }

    public static List<Integer> initialOreVeinsAmount()
    {
        List<Integer> tmp = new ArrayList<>();
        for(NCOre ore: all().values()) {
            tmp.add(ore.veinAmount);
        }
        return tmp;
    }

    public static List<Integer> initialOreMinHeight()
    {
        List<Integer> tmp = new ArrayList<>();
        for(NCOre ore: all().values()) {
            tmp.add(ore.height[0]);
        }
        return tmp;
    }

    public static List<Integer> initialOreMaxHeight()
    {
        List<Integer> tmp = new ArrayList<>();
        for(NCOre ore: all().values()) {
            tmp.add(ore.height[1]);
        }
        return tmp;
    }

    public static List<List<Integer>> initialOreDimensions() {
        List<List<Integer>> tmp = new ArrayList<>();
        for(NCOre ore: all().values()) {
            List<Integer> t = ore.dimensions;
            tmp.add(t);
        }
        return tmp;
    }
}