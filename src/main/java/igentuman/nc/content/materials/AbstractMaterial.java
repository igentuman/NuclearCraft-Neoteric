package igentuman.nc.content.materials;

import java.util.Collection;
import java.util.HashMap;

public abstract class AbstractMaterial {
    protected HashMap<String, NCMaterialProduct> all;
    protected HashMap<String, NCMaterialProduct> registered;
    protected boolean initialized = false;
    public Collection<NCMaterial> items;

    public String type;

    public HashMap<String, NCMaterialProduct> registered()
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

    public HashMap<String, NCMaterialProduct> all()
    {
        if(all == null) {
            all = new HashMap<>();
            for (NCMaterial m: items) {
                all.put(m.name, NCMaterialProduct.get(m.name, type));
            }
        }
        return all;
    }

}
