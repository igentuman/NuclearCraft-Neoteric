package igentuman.nc.content.materials;

import igentuman.nc.handler.config.CommonConfig;
import igentuman.nc.handler.config.MaterialsConfig;

import static igentuman.nc.handler.config.MaterialsConfig.MATERIAL_PRODUCTS;

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
            if(!MaterialsConfig.isLoaded()) {
                return this;
            }
            int id = Ores.all().keySet().stream().toList().indexOf(name);
            switch (type) {
                case "ingot":
                    registered = MATERIAL_PRODUCTS.INGOTS.get(id).get();
                    break;
                case "nugget":
                    registered = MATERIAL_PRODUCTS.NUGGET.get(id).get();
                    break;
                case "block":
                    registered = MATERIAL_PRODUCTS.BLOCK.get(id).get();
                    break;
                case "chunk":
                    registered = MATERIAL_PRODUCTS.RAW_CHUNKS.get(id).get();
                    break;
                case "plate":
                    registered = MATERIAL_PRODUCTS.PLATES.get(id).get();
                    break;
                case "dust":
                    registered = MATERIAL_PRODUCTS.DUSTS.get(id).get();
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
