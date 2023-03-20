package igentuman.nc.setup.materials;

import igentuman.nc.handler.config.CommonConfig;

public class NCMaterialProduct {
    public String name;
    public String type;
    private boolean initialized = false;
    private Boolean registered = true;

    public NCMaterialProduct(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public static NCMaterialProduct get(String name, String type)
    {
        return new NCMaterialProduct(name, type);
    }

    public NCMaterialProduct config()
    {
        if(!initialized) {
            if(!CommonConfig.isLoaded()) {
                return this;
            }
            int id = Ores.all().keySet().stream().toList().indexOf(name);
            registered = CommonConfig.OresConfig.REGISTER_ORE.get().get(id);
            initialized = true;
        }
        return this;
    }

    public boolean isRegistered() {

        return  registered;
    }

}
