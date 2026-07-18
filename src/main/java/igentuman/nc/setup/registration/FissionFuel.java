package igentuman.nc.setup.registration;

import igentuman.nc.compat.kubejs.NCKubeJsEvents;
import igentuman.nc.content.fuel.FuelDef;
import igentuman.nc.content.fuel.FuelManager;
import igentuman.nc.content.fuel.NCFuel;
import igentuman.nc.content.materials.Materials;
import igentuman.nc.item.ItemFuel;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.setup.registration.CreativeTabs.NC_ITEMS_TAB;
import static igentuman.nc.setup.registration.Registries.ITEMS;
import static igentuman.nc.setup.registration.Tags.*;
import static igentuman.nc.util.ModUtil.isKubeJsLoaded;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class FissionFuel {

    public static final Item.Properties ITEM_PROPERTIES = new Item.Properties().tab(NC_ITEMS_TAB);
    public static HashMap<List<String>, RegistryObject<Item>> NC_FUEL = new HashMap<>();
    public static HashMap<List<String>, RegistryObject<Item>> NC_DEPLETED_FUEL = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>>  NC_ISOTOPES = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>>  NC_WASTE = new HashMap<>();
    private static boolean initialized = false;
    // Store custom fuel definitions for recipe generation
    private static final List<FuelDef> CUSTOM_FUELS = new ArrayList<>();



    public static void init()
    {
        if(initialized) return;
        initialized = true;
        registerFuel();
        registerIsotopes();
        registerWaste();
    }
    
    /**
     * Get all custom fuels registered via KubeJS for recipe generation
     */
    public static List<FuelDef> getCustomFuels() {
        return new ArrayList<>(CUSTOM_FUELS);
    }

    public static void registerRuntimeFuels() {
        List<FuelDef> customFuels = new ArrayList<>();
        RegisterFissionFuelEvent event = new RegisterFissionFuelEvent(customFuels);
        MinecraftForge.EVENT_BUS.post(event);

        if(isKubeJsLoaded()) {
            NCKubeJsEvents.onFissionFuelRegister(event);
        }
        
        // Register items and recipes for custom fuels
        for (FuelDef fuelDef : event.getFuels()) {
            registerCustomFuel(fuelDef);
        }
    }
    
    private static void registerCustomFuel(FuelDef fuelDef) {
        String group = fuelDef.group;
        String name = fuelDef.name;
        
        // Store fuel definition for recipe generation
        CUSTOM_FUELS.add(fuelDef);
        
        // Register base fuel and depleted fuel
        NC_FUEL.put(List.of("fuel", group, name, ""), fuel(group, name, ""));
        REACTOR_FUEL_TAG.put(group + name, itemTag("reactor_fuel/" + group + "/" + name));
        
        NC_DEPLETED_FUEL.put(List.of("depleted", group, name, ""), depletedFuel(group, name, ""));
        REACTOR_DEPLETED_FUEL_TAG.put(group + name, itemTag("depleted_reactor_fuel/" + group + "/" + name));
        
        // Register variants (oxide, nitride, zirconium alloy, triso) if not special fuel
        NC_FUEL.put(List.of("fuel", group, name, "ox"), fuel(group, name, "_ox"));
        NC_FUEL.put(List.of("fuel", group, name, "ni"), fuel(group, name, "_ni"));
        NC_FUEL.put(List.of("fuel", group, name, "za"), fuel(group, name, "_za"));
        NC_FUEL.put(List.of("fuel", group, name, "tr"), fuel(group, name, "_tr"));

        NC_DEPLETED_FUEL.put(List.of("depleted", group, name, "ox"), depletedFuel(group, name, "_ox"));
        NC_DEPLETED_FUEL.put(List.of("depleted", group, name, "ni"), depletedFuel(group, name, "_ni"));
        NC_DEPLETED_FUEL.put(List.of("depleted", group, name, "za"), depletedFuel(group, name, "_za"));
        NC_DEPLETED_FUEL.put(List.of("depleted", group, name, "tr"), depletedFuel(group, name, "_tr"));

        // Add to FuelManager
        if (!FuelManager.all().containsKey(group)) {
            FuelManager.all().put(group, new HashMap<>());
        }
        FuelManager.all().get(group).put(name, NCFuel.of(fuelDef));
    }

    public static class RegisterFissionFuelEvent extends Event {
        public List<FuelDef> getFuels() {
            return fuels;
        }

        public void addFuel(FuelDef fuel) {
            this.fuels.add(fuel);
        }

        private final List<FuelDef> fuels;

        public RegisterFissionFuelEvent(List<FuelDef> fuels) {
            this.fuels = fuels;
        }
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
