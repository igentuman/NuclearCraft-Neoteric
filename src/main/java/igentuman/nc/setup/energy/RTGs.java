package igentuman.nc.setup.energy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import igentuman.nc.block.entity.energy.RTGBE;

public class RTGs {

    private static HashMap<String, RTGPrefab> all = new HashMap<>();
    private static HashMap<String, RTGPrefab> registered = new HashMap<>();

    public static HashMap<String, RTGPrefab> all() {
        if(all.isEmpty()) {
            //radiation in pRads
            all.put("uranium_rtg", new RTGPrefab("uranium_rtg",10, 56).setBlockEntity(RTGBE::new));
            all.put("americium_rtg", new RTGPrefab("americium_rtg",100, 578000).setBlockEntity(RTGBE::new));
            all.put("plutonium_rtg", new RTGPrefab("plutonium_rtg",400, 2000000).setBlockEntity(RTGBE::new));
            all.put("californium_rtg", new RTGPrefab("californium_rtg",2000, 19000000).setBlockEntity(RTGBE::new));
        }
        return all;
    }

    public static HashMap<String, RTGPrefab> registered() {
        if(registered.isEmpty()) {
            for(String name: all().keySet()) {
                if (all().get(name).config().isRegistered())
                    registered.put(name,all().get(name));
            }
        }
        return registered;
    }

    public static List<Boolean> initialRegistered() {
        List<Boolean> tmp = new ArrayList<>();
        for(String name: all().keySet()) {
            tmp.add(true);
        }
        return tmp;
    }

    public static List<Integer> initialPower() {
        List<Integer> tmp = new ArrayList<>();
        for(String name: all().keySet()) {
            tmp.add(all().get(name).getGeneration());
        }
        return tmp;
    }

    public static List<Integer> initialRadiation() {
        List<Integer> tmp = new ArrayList<>();
        for(String name: all().keySet()) {
            tmp.add(all().get(name).getRadiation());
        }
        return tmp;
    }
}