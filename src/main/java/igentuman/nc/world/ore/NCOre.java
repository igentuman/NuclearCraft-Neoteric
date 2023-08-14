package igentuman.nc.world.ore;

import igentuman.nc.handler.config.CommonConfig;
import igentuman.nc.content.materials.Ores;

import java.util.List;

import static igentuman.nc.handler.config.CommonConfig.ORE_CONFIG;

public class NCOre {
    public boolean initialized = false;
    public String name;
    public int color;
    public List<Integer> dimensions;
    public int veinSize;

    public int veinAmount;

    public boolean registered = true;

    public int[] height;
    private NCOre(String name) {
        this.name = name;
    }

    public NCOre vein(int size, int amount)
    {
        veinSize = size;
        veinAmount = amount;
        return this;
    }

    public NCOre height(int min, int max)
    {
        height = new int[]{min, max};
        return this;
    }

    public static NCOre get(String name)
    {
        return new NCOre(name);
    }

    public NCOre dim(Integer ...dim) {
        dimensions = List.of(dim);
        return this;
    }

    public NCOre config()
    {
        if(!initialized) {
            if(!CommonConfig.isLoaded()) {
                return this;
            }
            int id = Ores.all().keySet().stream().toList().indexOf(name);
            registered = ORE_CONFIG.REGISTER_ORE.get().get(id);
            height[0] = ORE_CONFIG.ORE_MIN_HEIGHT.get().get(id);
            height[1] = ORE_CONFIG.ORE_MAX_HEIGHT.get().get(id);
            dimensions = ORE_CONFIG.ORE_DIMENSIONS.get().get(id);
            initialized = true;
        }
        return this;
    }

    public boolean isRegistered() {

        return  registered;
    }
}
