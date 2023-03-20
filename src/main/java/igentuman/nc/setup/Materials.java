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
            all.put("tough_alloy", NCMaterial.metal("tough_alloy"));

        }
        return all;
    }

}
