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
            switch (type) {
                case "ingot":
                    registered = CommonConfig.MaterialProductsConfig.INGOTS.get().get(id);
                    break;
                case "nugget":
                    registered = CommonConfig.MaterialProductsConfig.NUGGET.get().get(id);
                    break;
                case "block":
                    registered = CommonConfig.MaterialProductsConfig.BLOCK.get().get(id);
                    break;
                case "chunk":
                    registered = CommonConfig.MaterialProductsConfig.CHUNKS.get().get(id);
                    break;
                case "plate":
                    registered = CommonConfig.MaterialProductsConfig.PLATES.get().get(id);
                    break;
                case "dust":
                    registered = CommonConfig.MaterialProductsConfig.DUSTS.get().get(id);
                    break;
            }
            initialized = true;
        }
        return this;
    }

    public boolean isRegistered() {

        return  registered;
    }

}
