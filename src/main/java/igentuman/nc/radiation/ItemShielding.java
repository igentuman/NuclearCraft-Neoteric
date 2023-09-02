package igentuman.nc.radiation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.handler.config.CommonConfig.RADIATION_CONFIG;
import static net.minecraft.world.item.Items.AIR;

public class ItemShielding {
    protected static HashMap<Item, Integer> shieldingItems = new HashMap<>();
    protected static boolean initialized = false;
    public static HashMap<Item, Integer> get()
    {
        return shieldingItems;
    }

    public static void init()
    {
        if(!shieldingItems.isEmpty()) {
            return;
        }

        for(String line: RADIATION_CONFIG.ARMOR_PROTECTION.get()) {
            String[] split = line.split("\\|");
            if(split.length != 2) {
                continue;
            }
            Item item = getItemByName(split[0].trim());
            if(item.equals(AIR)) {
                continue;
            }
            try {
                shieldingItems.put(item, Integer.parseInt(split[1].trim()));
            } catch (NumberFormatException ignored) {}
        }
    }

    public static void add(String item, int radiation)
    {
        Item toAdd = getItemByName(item);
        if(toAdd.equals(AIR)) {
            return;
        }
        shieldingItems.put(toAdd, radiation);
    }

    public static void add(Item item, int radiation)
    {
        shieldingItems.put(item, radiation);
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
        if(shieldingItems.containsKey(item)) {
            return shieldingItems.get(item);
        }
        return 0;
    }
}
