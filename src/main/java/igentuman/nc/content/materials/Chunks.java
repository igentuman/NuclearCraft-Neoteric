package igentuman.nc.content.materials;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Chunks {

    private static HashMap<String, NCMaterialProduct> all;
    private static HashMap<String, NCMaterialProduct> registered;
    private boolean initialized = false;

    public static HashMap<String, NCMaterialProduct> registered()
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

    public static HashMap<String, NCMaterialProduct> all()
    {
        if(all == null) {
            all = new HashMap<>();
            for (NCMaterial m: Materials.chunks().values()) {
                all.put(m.name, NCMaterialProduct.get(m.name, "chunk"));
            }
        }
        return all;
    }

    public static List<Boolean> initialRegistration()
    {
        List<Boolean> tmp = new ArrayList<>();
        for(NCMaterialProduct chunk: all().values()) {
            tmp.add(true);
        }
        return tmp;
    }



}