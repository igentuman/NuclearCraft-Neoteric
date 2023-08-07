package igentuman.nc.content.materials;

import igentuman.nc.setup.registration.Materials;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Plates {

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
            for (NCMaterial m: Materials.plates().values()) {
                all.put(m.name, NCMaterialProduct.get(m.name, "plate"));
            }
        }
        return all;
    }

    public static List<Boolean> initialRegistration()
    {
        List<Boolean> tmp = new ArrayList<>();
        for(NCMaterialProduct plate: all().values()) {
            tmp.add(true);
        }
        return tmp;
    }



}