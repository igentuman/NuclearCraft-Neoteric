package igentuman.nc.setup.registration.fuel;

import java.util.HashMap;

public class FuelManager {
    protected static HashMap<String, HashMap<String, NCFuel>> all;
    protected static HashMap<String, HashMap<String, NCFuel>> registered;
    protected static HashMap<String, Double> heat;
    protected static HashMap<String, Integer> efficiency;
    protected static HashMap<String, Integer> criticality;
    protected static HashMap<String, Integer> depletion;
    protected static HashMap<String, Integer> forge_energy;
    public static HashMap<String, HashMap<String, NCFuel>> all()
    {
        if(all == null) {
            all = new HashMap<>();
            HashMap<String, NCFuel> americium = new HashMap<>();

            americium.put("hea-242", NCFuel.of(new FuelDef("americium","hea-242", 768, 564, 32, 92, 140)));
            americium.put("lea-242", NCFuel.of(new FuelDef("americium","lea-242", 192, 94, 65, 74, 135)));
            all.put("americium", americium);

            HashMap<String, NCFuel> berkelium = new HashMap<>();
            berkelium.put("heb-248", NCFuel.of(new FuelDef("berkelium","heb-248", 540, 312, 32, 92, 170)));
            berkelium.put("leb-248", NCFuel.of(new FuelDef("berkelium","leb-248", 135, 52, 73, 108, 165)));
            all.put("berkelium", berkelium);

            HashMap<String, NCFuel> californium = new HashMap<>();
            californium.put("hecf-249", NCFuel.of(new FuelDef("californium","hecf-249", 864, 696, 30, 53, 180)));
            californium.put("hecf-251", NCFuel.of(new FuelDef("californium","hecf-251", 900, 720, 35, 100, 185)));
            californium.put("lecf-249", NCFuel.of(new FuelDef("californium","lecf-249", 216, 116, 60, 53, 175)));
            californium.put("lecf-251", NCFuel.of(new FuelDef("californium","lecf-251", 225, 120, 71, 100, 180)));
            all.put("californium", californium);

            HashMap<String, NCFuel> curium = new HashMap<>();
            curium.put("hecm-243", NCFuel.of(new FuelDef("curium", "hecm-243", 840, 672, 33, 75, 150)));
            curium.put("hecm-245", NCFuel.of(new FuelDef("curium", "hecm-245", 648, 408, 37, 121, 155)));
            curium.put("hecm-247", NCFuel.of(new FuelDef("curium", "hecm-247", 552, 324, 36, 108, 160)));
            curium.put("lecm-243", NCFuel.of(new FuelDef("curium", "lecm-243", 210, 112, 66, 75, 145)));
            curium.put("lecm-245", NCFuel.of(new FuelDef("curium", "lecm-245", 162, 68, 75, 121, 150)));
            curium.put("lecm-247", NCFuel.of(new FuelDef("curium", "lecm-247", 138, 54, 72, 108, 155)));
            all.put("curium", curium);

            HashMap<String, NCFuel> mix = new HashMap<>();
            mix.put("mix-239", NCFuel.of(new FuelDef("mixed", "mix-239", 155, 57.5, 94, 218, 105)));
            mix.put("mix-241", NCFuel.of(new FuelDef("mixed", "mix-241", 234, 97.5, 80, 151, 115)));
            all.put("mixed", mix);

            HashMap<String, NCFuel> neptunium = new HashMap<>();
            neptunium.put("hen-236", NCFuel.of(new FuelDef("neptunium", "hen-236", 360, 216, 35, 99, 115)));
            neptunium.put("len-236", NCFuel.of(new FuelDef("neptunium", "len-236", 90, 36, 70, 99, 110)));
            all.put("neptunium", neptunium);

            HashMap<String, NCFuel> plutonium = new HashMap<>();
            plutonium.put("hep-239", NCFuel.of(new FuelDef("plutonium", "hep-239", 420, 240, 49, 229, 145)));
            plutonium.put("hep-241", NCFuel.of(new FuelDef("plutonium", "hep-241", 660, 420, 42, 158, 130)));
            plutonium.put("lep-239", NCFuel.of(new FuelDef("plutonium", "lep-239", 105, 40, 99, 229, 150)));
            plutonium.put("lep-241", NCFuel.of(new FuelDef("plutonium", "lep-241", 165, 70, 84, 158, 125)));
            all.put("plutonium", plutonium);

            HashMap<String, NCFuel> thorium = new HashMap<>();
            thorium.put("tbu", NCFuel.of(new FuelDef("thorium", "tbu", 60, 18, 234, 720, 125)));
            all.put("thorium", thorium);

            HashMap<String, NCFuel> uranium = new HashMap<>();
            uranium.put("heu-233", NCFuel.of(new FuelDef("uranium", "heu-233", 576,360, 39, 133, 115)));
            uranium.put("heu-235", NCFuel.of(new FuelDef("uranium", "heu-235", 480, 300, 51, 240, 105)));
            uranium.put("leu-233", NCFuel.of(new FuelDef("uranium", "leu-233", 144, 60, 78, 133, 110)));
            uranium.put("leu-235", NCFuel.of(new FuelDef("uranium", "leu-235", 120, 50, 102, 240, 100)));
            all.put("uranium", uranium);

        }
        return all;
    }

    public static HashMap<String, Double> initialHeat()
    {
        if(heat == null) {
            heat = new HashMap<>();
            for(String name: all().keySet()) {
                for(String subItem: all().get(name).keySet()) {
                    heat.put(subItem, all().get(name).get(subItem).getOxide().heat);
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

    public static HashMap<String, Integer> initialForgeEnergy()
    {
        if(forge_energy == null) {
            forge_energy = new HashMap<>();
            for(String name: all().keySet()) {
                for(String subItem: all().get(name).keySet()) {
                    forge_energy.put(subItem, all().get(name).get(subItem).getOxide().forge_energy);
                }
            }
        }

        return forge_energy;
    }
}
