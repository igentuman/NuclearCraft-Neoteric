package igentuman.nc.content.materials;

import igentuman.nc.world.ore.NCOre;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Ores {

    private static HashMap<String, NCOre> all;
    private static HashMap<String, NCOre> registered;

    public static HashMap<String, NCOre> registered()
    {
        if(registered == null) {
            registered = new HashMap<>();
            for(String name: all().keySet()) {
                if(all().get(name).config().isRegistered()) {
                    registered.put(name, all().get(name));
                }
            }
        }
        return registered;
    }
    public static boolean isRegistered(String name)
    {
        return registered().containsKey(name);
    }

    public static HashMap<String, NCOre> all()
    {
        if(all == null) {
            int wasteland = -4848;
            all = new HashMap<>();
            for (NCMaterial m: Materials.ores().values()) {
                int min = 4;
                int max = 60;
                if(m.deepslate_ore) {
                    min = -60;
                }
                if(!m.normal_ore && !m.nether_ore && !m.end_ore) {
                    max = 0;
                }
                all.put(m.name, NCOre.get(m.name).vein(7, 3).height(min, max).dim(0, wasteland));
            }
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