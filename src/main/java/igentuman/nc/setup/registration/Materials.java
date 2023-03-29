package igentuman.nc.setup.registration;

import igentuman.nc.setup.registration.materials.MaterialsManager;
import igentuman.nc.setup.registration.materials.NCMaterial;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class Materials extends MaterialsManager {
    public static HashMap<String, NCMaterial> all()
    {
        if(all == null) {
            all = new HashMap<>();
            //ores and all basic stuff by default
            all.put("uranium", NCMaterial.ore("uranium").color(0xFF476447));
            all.put("thorium", NCMaterial.ore("thorium").color(0xFF2E2E2E));
            all.put("boron", NCMaterial.ore("boron").color(0xFF999999));
            all.put("silver", NCMaterial.ore("silver").color(0xFFA49CA6));
            all.put("lead", NCMaterial.ore("lead").ores(true, false, false, false).color(0xFF778887));
            all.put("tin", NCMaterial.ore("tin").ores(true, false, false, false).color(0xFFDFDFE8));
            all.put("zinc", NCMaterial.ore("zinc").ores(true, false, false, false).color(0xFFAFB0A6));
            all.put("magnesium", NCMaterial.ore("magnesium").color(0xFFF0D0ED));
            all.put("lithium", NCMaterial.ore("lithium"));
            all.put("cobalt", NCMaterial.ore("cobalt").color(0xFF5A6691));
            all.put("platinum", NCMaterial.ore("platinum").ores(false, true, false, false).color(0xFF6C939B));

            //ingots, nuggets, dusts...
            all.put("tough_alloy", NCMaterial.get("tough_alloy").define("ingot", "plate", "dust", "fluid").color(0xFF181322));
            all.put("copper", NCMaterial.get("copper").define("dust", "plate", "fluid").color(0xFFA86F32));
            all.put("iron", NCMaterial.get("iron").define("dust", "plate", "fluid").color(0xFF7D0707));
            all.put("purpur", NCMaterial.get("purpur").define("dust", "fluid").color(0xFF7E0399));
            all.put("gold", NCMaterial.get("gold").define("dust", "fluid").color(0xFFD4CD08));
            all.put("ferroboron", NCMaterial.get("ferroboron").define("ingot", "fluid").color(0xFF543636));
            all.put("enderium", NCMaterial.get("enderium").define("dust", "fluid").color(0xFF00856C));
            all.put("lapis", NCMaterial.get("lapis").define("dust", "fluid").color(0xFF04128F));
            all.put("hard_carbon", NCMaterial.get("hard_carbon").define("ingot", "plate", "dust", "fluid").color(0xFF164C5F));
            all.put("tin_silver", NCMaterial.get("tin_silver").define("ingot", "dust", "fluid").color(0xFFD3D3E9));
            all.put("steel", NCMaterial.alloy("steel").color(0xFF997B74));
            all.put("thermoconducting", NCMaterial.alloy("thermoconducting").define("ingot", "plate", "dust", "fluid").color(0xFF515C3C));
            all.put("zircaloy", NCMaterial.alloy("zircaloy").define("ingot", "dust", "fluid").color(0xFFD8D8D8));
            all.put("zirconium", NCMaterial.alloy("zirconium").color(0xFFC6C77F));
            all.put("zirconium_molybdenum", NCMaterial.alloy("zirconium_molybdenum").define("ingot", "dust", "fluid").color(0xFFB3B7BC));
            all.put("extreme", NCMaterial.alloy("extreme").define("ingot", "plate", "dust", "fluid").color(0xFF70293C));
            all.put("manganese", NCMaterial.alloy("manganese").define("ingot", "plate", "dust", "fluid").color(0xFF7284CC));
            all.put("magnesium_diboride", NCMaterial.alloy("magnesium_diboride").define("ingot", "fluid").color(0xFF26303D));
            all.put("manganese_oxide", NCMaterial.alloy("manganese_oxide").define("ingot", "dust", "fluid").color(0xFF7E9D7E));
            all.put("manganese_dioxide", NCMaterial.alloy("manganese_dioxide").define("ingot", "dust", "fluid").color(0xFF28211E));
            all.put("sic_sic_cmc", NCMaterial.alloy("sic_sic_cmc").define("ingot", "plate", "dust", "fluid").color(0xFF7A766C));
            all.put("lithium_manganese_dioxide", NCMaterial.alloy("lithium_manganese_dioxide").define("ingot", "dust", "fluid").color(0xFF696969));
            all.put("silicon_carbide", NCMaterial.alloy("silicon_carbide").define("ingot", "dust", "fluid").color(0xFF716E63));
            all.put("shibuichi", NCMaterial.alloy("shibuichi").define("ingot", "dust", "fluid").color(0xFFB9B3AE));
            all.put("beryllium", NCMaterial.alloy("beryllium").color(0xFFD7DFC6));
            all.put("bronze", NCMaterial.alloy("bronze").color(0xFFC78C47));
            all.put("corium", NCMaterial.alloy("corium").define("fluid").fluid(true, 3000).color(0xFF7C7C6F));
            all.put("electrum", NCMaterial.alloy("electrum").color(0xFFA0A15D));
            all.put("aluminum", NCMaterial.alloy("aluminum").color(0xFFABEBCB));
            all.put("graphite", NCMaterial.get("graphite").define("ingot", "dust", "block", "plate").color(0xFF292929));
            all.put("hsla_steel", NCMaterial.alloy("hsla_steel").define("ingot", "plate", "dust", "fluid").color(0xFF8174B0));
            //dusts
            all.put("bismuth", NCMaterial.dust("bismuth"));
            all.put("caesium_137", NCMaterial.dust("caesium_137"));
            all.put("europium_155", NCMaterial.dust("europium_155"));
            all.put("molybdenum", NCMaterial.dust("molybdenum"));
            all.put("polonium", NCMaterial.dust("polonium"));
            all.put("promethium_147", NCMaterial.dust("promethium_147"));
            all.put("protactinium_233", NCMaterial.dust("protactinium_233"));
            all.put("radium", NCMaterial.dust("radium"));
            all.put("ruthenium_106", NCMaterial.dust("ruthenium_106"));
            all.put("strontium_90", NCMaterial.dust("strontium_90"));
            all.put("tbp", NCMaterial.dust("tbp"));
            all.put("arsenic", NCMaterial.dust("arsenic"));
            all.put("boron_nitride", NCMaterial.dust("boron_nitride").with("gem"));
            all.put("carobbiite", NCMaterial.dust("carobbiite").with("gem"));
            all.put("coal", NCMaterial.dust("coal"));
            all.put("diamond", NCMaterial.dust("diamond"));
            all.put("emerald", NCMaterial.dust("emerald"));
            all.put("end_stone", NCMaterial.dust("end_stone"));
            all.put("fluorite", NCMaterial.dust("fluorite").with("gem"));
            all.put("obsidian", NCMaterial.dust("obsidian"));
            all.put("quartz", NCMaterial.dust("quartz"));
            all.put("rhodochrosite", NCMaterial.dust("rhodochrosite").with("gem"));
            all.put("sulfur", NCMaterial.dust("sulfur").fluid(true, 500).color(0xFFDEDE7A));
            all.put("villiaumite", NCMaterial.dust("villiaumite").with("gem"));
            all.put("alugentum", NCMaterial.dust("alugentum"));
            all.put("borax", NCMaterial.dust("borax"));
            all.put("c_mn_blend", NCMaterial.dust("c_mn_blend"));
            all.put("calcium_sulfate", NCMaterial.dust("calcium_sulfate"));
            all.put("crystal_binder", NCMaterial.dust("crystal_binder"));
            all.put("dimensional_blend", NCMaterial.dust("dimensional_blend"));
            all.put("energetic_blend", NCMaterial.dust("energetic_blend"));
            all.put("irradiated_borax", NCMaterial.dust("irradiated_borax"));
            all.put("potassium_fluoride", NCMaterial.dust("potassium_fluoride"));
            all.put("potassium_hydroxide", NCMaterial.dust("potassium_hydroxide"));
            all.put("sodium_fluoride", NCMaterial.dust("sodium_fluoride"));
            all.put("sodium_hydroxide", NCMaterial.dust("sodium_hydroxide"));

        }
        return all;
    }

    public static List<String> isotopes()
    {
        return  Arrays.asList(
                "americium/241",
                "americium/242",
                "americium/243",
                "berkelium/247",
                "berkelium/248",
                "boron/10",
                "boron/11",
                "californium/249",
                "californium/250",
                "californium/251",
                "californium/252",
                "curium/243",
                "curium/245",
                "curium/246",
                "curium/247",
                "lithium/6",
                "lithium/7",
                "neptunium/236",
                "neptunium/237",
                "plutonium/238",
                "plutonium/239",
                "plutonium/241",
                "plutonium/242",
                "thorium/230",
                "thorium/232",
                "uranium/233",
                "uranium/235",
                "uranium/238"
        );
    }

}
