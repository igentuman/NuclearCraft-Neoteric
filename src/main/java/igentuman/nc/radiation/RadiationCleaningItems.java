package igentuman.nc.radiation;

import net.minecraft.util.ResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.handler.config.RadiationConfig.RADIATION_CONFIG;
import static net.minecraft.item.Items.AIR;

public class RadiationCleaningItems {
    protected static HashMap<Item, Integer> radiationMap = new HashMap<>();
    protected static boolean initialized = false;
    public static HashMap<Item, Integer> get()
    {
        return radiationMap;
    }

    public static void init()
    {
        if(!radiationMap.isEmpty()) {
            return;
        }
        for(String line: RADIATION_CONFIG.RADIATION_REMOVAL_ITEMS.get()) {
            String[] split = line.split("\\|");
            if(split.length != 2) {
                continue;
            }
            Item item = getItemByName(split[0].trim());
            if(item.equals(AIR)) {
                continue;
            }
            try {
                radiationMap.put(item, Integer.parseInt(split[1].trim()));
            } catch (NumberFormatException ignored) {}
        }

    }

    public static void add(String item, int radiation)
    {
        Item toAdd = getItemByName(item);
        if(toAdd.equals(AIR)) {
            return;
        }
        radiationMap.put(toAdd, radiation);
    }

    public static void add(Item item, int radiation)
    {
        radiationMap.put(item, radiation);
    }

    protected static Item getItemByName(String name)
    {
        if(!name.contains(":")) {
            name = MODID +":" + name;
        }
        ResourceLocation itemKey = new ResourceLocation(name.replace("/", "_"));
        return ForgeRegistries.ITEMS.getValue(itemKey);
    }

    public static int byItem(Item item) {
        if(!initialized) {
            init();
            initialized = true;
        }
        if(radiationMap.containsKey(item)) {
            return radiationMap.get(item);
        }
        return 0;
    }
}
