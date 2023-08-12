package igentuman.nc.content.materials;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static net.minecraft.world.item.Items.AIR;

public class ItemRadiation {
    protected static HashMap<Item, Double> radiationMap = new HashMap<>();
    protected static boolean initialized = false;
    public static HashMap<Item, Double> get()
    {
        return radiationMap;
    }

    public static void init()
    {
        if(!radiationMap.isEmpty()) {
            return;
        }

        for(String name: Materials.isotopes()) {
            for(String type: List.of("", "_ox", "_ni", "_za", "_tr")) {
                add(name+type, Materials.isotopes.get(name));
            }
        }
    }

    public static void add(String item, double radiation)
    {
        Item toAdd = getItemByName(item);
        if(toAdd.equals(AIR)) {
            return;
        }
        radiationMap.put(toAdd, radiation);
    }

    public static void add(Item item, double radiation)
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

    public static double byItem(Item item) {
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
