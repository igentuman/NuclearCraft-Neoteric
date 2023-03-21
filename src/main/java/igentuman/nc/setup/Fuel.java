package igentuman.nc.setup;

import igentuman.nc.item.ItemFuel;
import igentuman.nc.setup.fuel.FuelManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;

public class Fuel {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static TagKey<Item> ISOTOPE_TAG = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", "isotopes"));
    public static TagKey<Item> NC_ISOTOPE_TAG = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(MODID, "isotopes"));
    public static TagKey<Item> NC_FUEL_TAG = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(MODID, "reactor_fuel"));
    public static TagKey<Item> NC_DEPLETED_FUEL_TAG = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(MODID, "reactor_fuel"));
    public static TagKey<Item> NC_FUELS_TAG = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(MODID, "reactor_fuel"));

    public static final Item.Properties ITEM_PROPERTIES = new Item.Properties().tab(ModSetup.ITEM_GROUP);


    public static HashMap<List<String>, RegistryObject<Item>> NC_FUEL = new HashMap<>();
    public static HashMap<List<String>, RegistryObject<Item>> NC_DEPLETED_FUEL = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>>  NC_ISOTOPES = new HashMap<>();

    public static void init()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        registerFuel();
        registerIsotopes();
    }

    private static void registerFuel() {
        for (String name: FuelManager.all().keySet()) {
            for(String subType: FuelManager.all().get(name).keySet()) {
                NC_FUEL.put(List.of("fuel", name, subType, ""),
                        ITEMS.register("fuel_"+name+"_"+subType.replace("-","_"),
                                () -> new ItemFuel(ITEM_PROPERTIES, FuelManager.all().get(name).get(subType).getDefault())));
                NC_FUEL.put(List.of("fuel", name, subType, "ox"),
                        ITEMS.register("fuel_"+name+"_"+subType.replace("-","_")+"_ox",
                                () -> new ItemFuel(ITEM_PROPERTIES, FuelManager.all().get(name).get(subType).getOxide())));
                NC_FUEL.put(List.of("fuel", name, subType, "ni"),
                        ITEMS.register("fuel_"+name+"_"+subType.replace("-","_")+"_ni",
                                () -> new ItemFuel(ITEM_PROPERTIES, FuelManager.all().get(name).get(subType).getNitride())));
                NC_FUEL.put(List.of("fuel", name, subType, "za"),
                        ITEMS.register("fuel_"+name+"_"+subType.replace("-","_")+"_za",
                                () -> new ItemFuel(ITEM_PROPERTIES, FuelManager.all().get(name).get(subType).getZirconiumAlloy())));
                NC_FUEL.put(List.of("fuel", name, subType, "tr"),
                        ITEMS.register("fuel_"+name+"_"+subType.replace("-","_")+"_tr",
                                () -> new ItemFuel(ITEM_PROPERTIES, FuelManager.all().get(name).get(subType).getTriso())));

                NC_DEPLETED_FUEL.put(List.of("depleted", name, subType, ""),
                        ITEMS.register("depleted_fuel_"+name+"_"+subType.replace("-","_"),
                                () -> new Item(ITEM_PROPERTIES)));
                NC_DEPLETED_FUEL.put(List.of("depleted", name, subType, "ox"),
                        ITEMS.register("depleted_fuel_"+name+"_"+subType.replace("-","_")+"_ox",
                                () -> new Item(ITEM_PROPERTIES)));
                NC_DEPLETED_FUEL.put(List.of("depleted", name, subType, "ni"),
                        ITEMS.register("depleted_fuel_"+name+"_"+subType.replace("-","_")+"_ni",
                                () -> new Item(ITEM_PROPERTIES)));
                NC_DEPLETED_FUEL.put(List.of("depleted", name, subType, "za"),
                        ITEMS.register("depleted_fuel_"+name+"_"+subType.replace("-","_")+"_za",
                                () -> new Item(ITEM_PROPERTIES)));
                NC_DEPLETED_FUEL.put(List.of("depleted", name, subType, "tr"),
                        ITEMS.register("depleted_fuel_"+name+"_"+subType.replace("-","_")+"_tr",
                                () -> new Item(ITEM_PROPERTIES)));
            }
        }
    }

    private static void registerIsotopes() {
        List<String> parts = Arrays.asList(
                "americium/241",
                "americium/242",
                "americium/243",
                "berkelium/247",
                "berkelium/248",
                "boron/10",
                "boron/11",
                "californium/249",
                "californium/250",
                "californium/251",
                "californium/252",
                "curium/243",
                "curium/245",
                "curium/246",
                "curium/247",
                "lithium/6",
                "lithium/7",
                "neptunium/236",
                "neptunium/237",
                "plutonium/238",
                "plutonium/239",
                "plutonium/241",
                "plutonium/242",
                "thorium/tbu",
                "thorium/232",
                "uranium/233",
                "uranium/235",
                "uranium/238"
        );
        for(String name: parts) {
            for(String type: new String[]{"", "_za", "_ox","_ni"}) {
                NC_ISOTOPES.put(name+type, ITEMS.register(name.replace("/", "_")+type, () -> new Item(ITEM_PROPERTIES)));
            }
        }
    }

}
