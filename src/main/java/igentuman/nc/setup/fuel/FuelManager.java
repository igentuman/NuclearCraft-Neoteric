package igentuman.nc.setup.fuel;

import java.util.HashMap;

public class FuelManager {
    protected static HashMap<String, HashMap<String, NCFuel>> all;
    protected static HashMap<String, HashMap<String, NCFuel>> registered;
    protected static HashMap<String, Integer> heat;
    protected static HashMap<String, Integer> efficiency;
    protected static HashMap<String, Integer> criticality;
    protected static HashMap<String, Integer> depletion;
    public static HashMap<String, HashMap<String, NCFuel>> all()
    {
        if(all == null) {
            all = new HashMap<>();
            HashMap<String, NCFuel> americium = new HashMap<>();

            americium.put("hea-242", NCFuel.of(new FuelDef("hea-242", 1170, 32, 92, 140)));
            americium.put("lea-242", NCFuel.of(new FuelDef("lea-242", 390, 65, 74, 135)));
            all.put("americium", americium);

            HashMap<String, NCFuel> berkelium = new HashMap<>();
            berkelium.put("heb-248", NCFuel.of(new FuelDef("heb-248", 798, 32, 92, 170)));
            berkelium.put("leb-248", NCFuel.of(new FuelDef("leb-248", 266, 73, 108, 165)));
            all.put("berkelium", berkelium);

            HashMap<String, NCFuel> californium = new HashMap<>();
            californium.put("hecf-249", NCFuel.of(new FuelDef("hecf-249", 1620, 30, 53, 180)));
            californium.put("hecf-251", NCFuel.of(new FuelDef("hecf-251", 864, 35, 100, 185)));
            californium.put("lecf-249", NCFuel.of(new FuelDef("lecf-249", 540, 60, 53, 175)));
            californium.put("lecf-251", NCFuel.of(new FuelDef("lecf-251", 288, 71, 100, 180)));
            all.put("californium", californium);

            HashMap<String, NCFuel> curium = new HashMap<>();
            curium.put("hecm-243", NCFuel.of(new FuelDef("hecm-243", 1152, 33, 75, 150)));
            curium.put("hecm-245", NCFuel.of(new FuelDef("hecm-245", 714, 37, 121, 155)));
            curium.put("hecm-247", NCFuel.of(new FuelDef("hecm-247", 804, 36, 108, 160)));
            curium.put("lecm-243", NCFuel.of(new FuelDef("lecm-243", 384, 66, 75, 145)));
            curium.put("lecm-245", NCFuel.of(new FuelDef("lecm-245", 238, 75, 121, 150)));
            curium.put("lecm-247", NCFuel.of(new FuelDef("lecm-247", 268, 72, 108, 155)));
            all.put("curium", curium);

            HashMap<String, NCFuel> mix = new HashMap<>();
            mix.put("mix-239", NCFuel.of(new FuelDef("mix-239", 132, 94, 218, 105)));
            mix.put("mix-241", NCFuel.of(new FuelDef("mix-241", 192, 80, 151, 115)));
            all.put("mixed", mix);

            HashMap<String, NCFuel> neptunium = new HashMap<>();
            neptunium.put("hen-236", NCFuel.of(new FuelDef("hen-236", 876, 35, 99, 115)));
            neptunium.put("len-236", NCFuel.of(new FuelDef("len-236", 292, 70, 99, 110)));
            all.put("neptunium", neptunium);

            HashMap<String, NCFuel> plutonium = new HashMap<>();
            plutonium.put("hep-239", NCFuel.of(new FuelDef("hep-239", 378, 49, 229, 145)));
            plutonium.put("hep-241", NCFuel.of(new FuelDef("hep-241", 546, 42, 158, 130)));
            plutonium.put("lep-239", NCFuel.of(new FuelDef("lep-239", 126, 99, 229, 150)));
            plutonium.put("lep-241", NCFuel.of(new FuelDef("lep-241", 182, 84, 158, 125)));
            all.put("plutonium", plutonium);

            HashMap<String, NCFuel> thorium = new HashMap<>();
            thorium.put("tbu", NCFuel.of(new FuelDef("tbu", 40, 234, 720, 125)));
            all.put("thorium", thorium);

            HashMap<String, NCFuel> uranium = new HashMap<>();
            uranium.put("heu-233", NCFuel.of(new FuelDef("heu-233", 648, 39, 133, 115)));
            uranium.put("heu-235", NCFuel.of(new FuelDef("heu-235", 360, 51, 240, 105)));
            uranium.put("leu-233", NCFuel.of(new FuelDef("leu-233", 216, 78, 133, 110)));
            uranium.put("leu-235", NCFuel.of(new FuelDef("leu-235", 120, 102, 240, 100)));
            all.put("uranium", uranium);

        }
        return all;
    }

    public static HashMap<String, Integer> initialHeat()
    {
        if(heat == null) {
            heat = new HashMap<>();
            for(String name: all().keySet()) {
                for(String subItem: all().get(name).keySet()) {
                    heat.put(subItem, all().get(name).get(subItem).getOxide().efficiency);
                }
            }
        }

        return heat;
    }

    public static HashMap<String, Integer> initialDepletion()
    {
        if(depletion == null) {
            depletion = new HashMap<>();
            for(String name: all().keySet()) {
                for(String subItem: all().get(name).keySet()) {
                    depletion.put(subItem, all().get(name).get(subItem).getOxide().depletion);
                }
            }
        }

        return depletion;
    }

    public static HashMap<String, Integer> initialCriticality()
    {
        if(criticality == null) {
            criticality = new HashMap<>();
            for(String name: all().keySet()) {
                for(String subItem: all().get(name).keySet()) {
                    criticality.put(subItem, all().get(name).get(subItem).getOxide().criticality);
                }
            }
        }

        return criticality;
    }

    public static HashMap<String, Integer> initialEfficiency()
    {
        if(efficiency == null) {
            efficiency = new HashMap<>();
            for(String name: all().keySet()) {
                for(String subItem: all().get(name).keySet()) {
                    efficiency.put(subItem, all().get(name).get(subItem).getOxide().efficiency);
                }
            }
        }

        return efficiency;
    }
}
