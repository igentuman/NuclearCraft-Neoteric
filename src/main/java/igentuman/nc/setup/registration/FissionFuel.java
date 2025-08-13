package igentuman.nc.setup.registration;

import igentuman.nc.content.materials.Materials;
import igentuman.nc.item.ItemFuel;
import igentuman.nc.content.fuel.FuelManager;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.setup.registration.Registries.ITEMS;
import static igentuman.nc.setup.registration.Tags.*;

public class FissionFuel {

    public static final Item.Properties ITEM_PROPERTIES = new Item.Properties();
    public static HashMap<List<String>, RegistryObject<Item>> NC_FUEL = new HashMap<>();
    public static HashMap<List<String>, RegistryObject<Item>> NC_DEPLETED_FUEL = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>>  NC_ISOTOPES = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>>  NC_WASTE = new HashMap<>();

    public static void init()
    {
        registerFuel();
        registerIsotopes();
        registerWaste();
    }

    public static RegistryObject<Item> fuel(String name, String type, String subType)
    {
        return ITEMS.register("fuel_"+name+"_"+type.replace("-","_")+subType,
                () -> new ItemFuel(ITEM_PROPERTIES, name, type, subType));
    }
    public static RegistryObject<Item> depletedFuel(String name, String type, String subType)
    {
        return ITEMS.register("depleted_fuel_"+name+"_"+type.replace("-","_")+subType,
                () -> new Item(ITEM_PROPERTIES));
    }



    private static void registerFuel() {
        for (String name: FuelManager.all().keySet()) {
            for(String subType: FuelManager.all().get(name).keySet()) {
                NC_FUEL.put(List.of("fuel", name, subType, ""), fuel(name, subType, ""));
                REACTOR_FUEL_TAG.put(name + subType, itemTag("reactor_fuel/" + name + "/" + subType));

                NC_DEPLETED_FUEL.put(List.of("depleted", name, subType, ""), depletedFuel(name, subType, ""));
                REACTOR_DEPLETED_FUEL_TAG.put(name + subType, itemTag("depleted_reactor_fuel/" + name + "/" + subType));

                if(name.matches("xenorium.*|quantite.*")) break;
                NC_FUEL.put(List.of("fuel", name, subType, "ox"), fuel(name, subType, "_ox"));
                NC_FUEL.put(List.of("fuel", name, subType, "ni"), fuel(name, subType, "_ni"));
                NC_FUEL.put(List.of("fuel", name, subType, "za"), fuel(name, subType, "_za"));
                NC_FUEL.put(List.of("fuel", name, subType, "tr"), fuel(name, subType, "_tr"));

                NC_DEPLETED_FUEL.put(List.of("depleted", name, subType, "ox"), depletedFuel(name, subType, "_ox"));
                NC_DEPLETED_FUEL.put(List.of("depleted", name, subType, "ni"), depletedFuel(name, subType, "_ni"));
                NC_DEPLETED_FUEL.put(List.of("depleted", name, subType, "za"), depletedFuel(name, subType, "_za"));
                NC_DEPLETED_FUEL.put(List.of("depleted", name, subType, "tr"), depletedFuel(name, subType, "_tr"));
            }
        }
    }

    public static void registerWaste() {
        for(String name: Materials.waste()) {
            NC_WASTE.put(name, ITEMS.register(name+"_spallation_waste", () -> new Item(ITEM_PROPERTIES)));
            NC_WASTE_TAG.put(name, itemTag("waste/" + name));
        }
    }

    public static void registerIsotopes() {
        for(String name: Materials.isotopes()) {
            for(String type: new String[]{"", "_za", "_ox","_ni"}) {
                NC_ISOTOPES.put(name+type, ITEMS.register(name.replace("/", "_")+type, () -> new Item(ITEM_PROPERTIES)));
                NC_ISOTOPE_TAG.put(name, itemTag("isotopes/" + name));
                if(name.matches("xenorium.*|quantite|beryllium.*|calcium.*|iridium.*|magnesium.*|sodium.*|cobalt.*")) {
                    break;
                }
            }
        }
    }
}
