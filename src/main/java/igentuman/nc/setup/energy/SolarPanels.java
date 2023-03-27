package igentuman.nc.setup.energy;

import igentuman.nc.block.entity.energy.solar.AdvancedSolarBE;
import igentuman.nc.block.entity.energy.solar.BasicSolarBE;
import igentuman.nc.block.entity.energy.solar.DuSolarBE;
import igentuman.nc.block.entity.energy.solar.EliteSolarBE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SolarPanels {

    private static HashMap<String, SolarPanelPrefab> all = new HashMap<>();
    private static HashMap<String, SolarPanelPrefab> registered = new HashMap<>();

    public static HashMap<String, SolarPanelPrefab> all() {
        if(all.isEmpty()) {
            all.put("basic", new SolarPanelPrefab("basic",5).setBlockEntity(BasicSolarBE::new));
            all.put("advanced", new SolarPanelPrefab("advanced",20).setBlockEntity(AdvancedSolarBE::new));
            all.put("du", new SolarPanelPrefab("du",80).setBlockEntity(DuSolarBE::new));
            all.put("elite", new SolarPanelPrefab("elite",320).setBlockEntity(EliteSolarBE::new));
        }
        return all;
    }

    public static HashMap<String, SolarPanelPrefab> registered() {
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

    public static String getCode(String name) {
        for(String code: all().keySet()) {
            if(name.equals("solar_panel_"+code)) {
                return "solar_panel/"+code;
            }
        }
        return "";
    }
}
