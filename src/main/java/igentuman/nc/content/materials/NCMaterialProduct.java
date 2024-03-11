package igentuman.nc.content.materials;

import igentuman.nc.handler.config.CommonConfig;

import java.util.Arrays;

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
            if(!CommonConfig.isLoaded()) {
                return this;
            }
            int id = Arrays.asList(Ores.all().keySet().stream().toArray()).indexOf(name);
            switch (type) {
                case "ingot":
                    registered = MATERIAL_PRODUCTS.INGOTS.get().get(id);
                    break;
                case "nugget":
                    registered = MATERIAL_PRODUCTS.NUGGET.get().get(id);
                    break;
                case "block":
                    registered = MATERIAL_PRODUCTS.BLOCK.get().get(id);
                    break;
                case "chunk":
                    registered = MATERIAL_PRODUCTS.CHUNKS.get().get(id);
                    break;
                case "plate":
                    registered = MATERIAL_PRODUCTS.PLATES.get().get(id);
                    break;
                case "dust":
                    registered = MATERIAL_PRODUCTS.DUSTS.get().get(id);
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
