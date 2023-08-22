package igentuman.nc.radiation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.NCArmor.*;
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
        add(HAZMAT_MASK.get(), 3);
        add(HAZMAT_CHEST.get(), 5);
        add(HAZMAT_PANTS.get(), 4);
        add(HAZMAT_BOOTS.get(), 2);

        add(HEV_HELMET.get(), 5);
        add(HEV_CHEST.get(), 7);
        add(HEV_PANTS.get(), 6);
        add(HEV_BOOTS.get(), 4);
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
