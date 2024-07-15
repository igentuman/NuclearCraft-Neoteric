package igentuman.nc.content.materials;

import static igentuman.nc.handler.config.MaterialsConfig.MATERIAL_PRODUCTS;

public class NCMaterialProduct {
    protected String name;
    protected String type;
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
        int id = 0;
        if(!initialized) {
            switch (type) {
                case "ingot":
                    id = Materials.ingots().keySet().stream().toList().indexOf(name);
                    registered = MATERIAL_PRODUCTS.INGOTS.get(id).get();
                    break;
                case "nugget":
                    id = Materials.nuggets().keySet().stream().toList().indexOf(name);
                    registered = MATERIAL_PRODUCTS.NUGGETS.get(id).get();
                    break;
                case "block":
                    id = Materials.blocks().keySet().stream().toList().indexOf(name);
                    registered = MATERIAL_PRODUCTS.BLOCK.get(id).get();
                    break;
                case "chunk":
                    id = Materials.chunks().keySet().stream().toList().indexOf(name);
                    registered = MATERIAL_PRODUCTS.RAW_CHUNKS.get(id).get();
                    break;
                case "plate":
                    id = Materials.plates().keySet().stream().toList().indexOf(name);
                    registered = MATERIAL_PRODUCTS.PLATES.get(id).get();
                    break;
                case "dust":
                    id = Materials.dusts().keySet().stream().toList().indexOf(name);
                    registered = MATERIAL_PRODUCTS.DUSTS.get(id).get();
                    break;
                case "gem":
                    id = Materials.gems().keySet().stream().toList().indexOf(name);
                    registered = MATERIAL_PRODUCTS.GEMS.get(id).get();
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
