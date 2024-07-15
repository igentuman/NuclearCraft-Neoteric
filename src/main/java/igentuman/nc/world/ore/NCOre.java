package igentuman.nc.world.ore;

import igentuman.nc.NuclearCraft;
import igentuman.nc.handler.config.CommonConfig;
import igentuman.nc.content.materials.Ores;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static igentuman.nc.handler.config.MaterialsConfig.ORE_CONFIG;
import static igentuman.nc.setup.registration.NCBlocks.ORE_BLOCKS;

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
            try {
                int id = Ores.all().keySet().stream().toList().indexOf(name);
                registered = (boolean) ORE_CONFIG.ORES.get(name).get(0).get();
                veinSize = (int) ORE_CONFIG.ORES.get(name).get(2).get();
                height[0] = (int) ORE_CONFIG.ORES.get(name).get(3).get();
                height[1] = (int) ORE_CONFIG.ORES.get(name).get(4).get();
                initialized = true;
                try {
                    dimensions = (List<Integer>) ((ArrayList<?>) ORE_CONFIG.ORES.get(name).get(1).get()).stream().toList();
                } catch (Exception e) {
                    NuclearCraft.LOGGER.warn("Error while loading dimensions ore config for " + name + "!");
                }
            } catch (Exception e) {
                NuclearCraft.LOGGER.error("Error while loading ore config for " + name + "!");
            }
        }
        return this;
    }

    public boolean isRegistered() {

        return  registered;
    }

    public Block block(String suffix) {
        return ORE_BLOCKS.get(name+suffix).get();
    }

    public Block block() {
        return ORE_BLOCKS.get(name).get();
    }
}
