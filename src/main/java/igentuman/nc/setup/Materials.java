package igentuman.nc.setup;

import igentuman.nc.setup.materials.MaterialsManager;
import igentuman.nc.setup.materials.NCMaterial;

import java.util.HashMap;


public class Materials extends MaterialsManager {
    public static HashMap<String, NCMaterial> all()
    {
        if(all == null) {
            all = new HashMap<>();
            //ores and all basic stuff by default
            all.put("uranium", NCMaterial.ore("uranium"));
            all.put("thorium", NCMaterial.ore("thorium"));
            all.put("boron", NCMaterial.ore("boron"));
            all.put("lead", NCMaterial.ore("lead").ores(true, false, false, false));
            all.put("tin", NCMaterial.ore("tin").ores(true, false, false, false));
            all.put("zinc", NCMaterial.ore("zinc").ores(true, false, false, false));
            all.put("magnesium", NCMaterial.ore("magnesium"));
            all.put("lithium", NCMaterial.ore("lithium"));
            all.put("cobalt", NCMaterial.ore("cobalt"));
            all.put("platinum", NCMaterial.ore("platinum").ores(false, true, false, false));

            //ingots, nuggets, dusts...
            all.put("tough_alloy", NCMaterial.get("tough_alloy").define("ingot", "plate", "dust", "fluid"));
            all.put("hard_carbon", NCMaterial.get("hard_carbon").define("ingot", "plate", "dust", "fluid"));
            all.put("tin_silver", NCMaterial.get("tin_silver").define("ingot", "dust", "fluid"));
            all.put("steel", NCMaterial.alloy("steel"));
            all.put("thermoconducting", NCMaterial.alloy("thermoconducting").define("ingot", "plate", "dust", "fluid"));
            all.put("zircaloy", NCMaterial.alloy("zircaloy").define("ingot", "dust", "fluid"));
            all.put("zirconium", NCMaterial.alloy("zirconium"));
            all.put("zirconium_molybdenum", NCMaterial.alloy("zirconium_molybdenum").define("ingot", "dust", "fluid"));
            all.put("extreme", NCMaterial.alloy("extreme").define("ingot", "plate", "dust", "fluid"));
            all.put("manganese", NCMaterial.alloy("manganese").define("ingot", "plate", "dust", "fluid"));
            all.put("manganese_oxide", NCMaterial.alloy("manganese_oxide").define("ingot", "dust", "fluid"));
            all.put("manganese_dioxide", NCMaterial.alloy("manganese_dioxide").define("ingot", "dust", "fluid"));
            all.put("sic_sic_cmc", NCMaterial.alloy("sic_sic_cmc").define("ingot", "plate", "dust", "fluid"));
            all.put("lithium_manganese_dioxide", NCMaterial.alloy("lithium_manganese_dioxide").define("ingot", "dust", "fluid"));
            all.put("silicon_carbide", NCMaterial.alloy("silicon_carbide").define("ingot", "dust", "fluid"));
            all.put("shibuichi", NCMaterial.alloy("shibuichi").define("ingot", "dust", "fluid"));
            all.put("beryllium", NCMaterial.alloy("beryllium"));
            all.put("bronze", NCMaterial.alloy("bronze"));
            all.put("electrum", NCMaterial.alloy("electrum"));
            all.put("aluminum", NCMaterial.alloy("aluminum"));
            all.put("graphite", NCMaterial.get("graphite").define("ingot", "dust", "block", "plate"));
            all.put("hsla_steel", NCMaterial.alloy("hsla_steel").define("ingot", "plate", "dust", "fluid"));


        }
        return all;
    }

}
