package igentuman.nc.radiation;

import igentuman.nc.content.fuel.FuelManager;
import igentuman.nc.content.materials.Materials;
import igentuman.nc.content.energy.RTGs;
import igentuman.nc.setup.registration.FissionFuel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.content.materials.Materials.*;
import static igentuman.nc.handler.config.RadiationConfig.RADIATION_CONFIG;
import static igentuman.nc.setup.registration.NCEnergyBlocks.ENERGY_BLOCKS;
import static igentuman.nc.setup.registration.NCItems.NC_DUSTS;
import static igentuman.nc.setup.registration.NCItems.NC_INGOTS;
import static igentuman.nc.util.NcUtils.getNCBlock;
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
        radiationMap.put(NC_DUSTS.get(protactinium_233).get(), 1D);
        radiationMap.put(NC_DUSTS.get(strontium_90).get(), 0.034D);
        radiationMap.put(NC_DUSTS.get(ruthenium_106).get(), 0.98D);
        radiationMap.put(NC_DUSTS.get(polonium).get(), 0.293D);
        radiationMap.put(NC_DUSTS.get(promethium_147).get(), 0.38D);
        radiationMap.put(NC_DUSTS.get(europium_155).get(), 0.21D);
        radiationMap.put(NC_INGOTS.get(uranium).get(), 0.00007D);
        radiationMap.put(NC_INGOTS.get(thorium).get(), 0.00005D);
        for(String name: RTGs.registered().keySet()) {
            radiationMap.put(ENERGY_BLOCKS.get(name).get().asItem(), ((double)RTGs.registered().get(name).config().getRadiation()/1000000000));
        }
        for(String line: RADIATION_CONFIG.ITEM_RADIATION.get()) {
            String[] split = line.split("\\|");
            if(split.length != 2) {
                continue;
            }
            Item item = getItemByName(split[0].trim());
            if(item.equals(AIR)) {
                continue;
            }
            try {
                radiationMap.put(item, Double.parseDouble(split[1].trim())/1000000000);
            } catch (NumberFormatException ignored) {}
        }

        for(String name: Materials.isotopes()) {
            for(String type: List.of("", "_ox", "_ni", "_za", "_tr")) {
                add(name+type, Materials.isotopes.get(name));
            }
        }

        add(getNCBlock("americium241").asItem(), 0.01D);
        add(getNCBlock("uranium238").asItem(),0.000005D);
        add(getNCBlock("californium250").asItem(),3D);
        add(getNCBlock("plutonium238").asItem(), 0.034D);

        for (String name: FuelManager.all().keySet()) {
            for(String subType: FuelManager.all().get(name).keySet()) {
                for(String type: List.of("", "ox", "ni", "za", "tr")) {
                    int isotope1Cnt = 1;
                    int isotope2Cnt = 8;
                    if(subType.substring(0,1).equalsIgnoreCase("h")) {
                        isotope1Cnt = 3;
                        isotope2Cnt = 6;
                    }
                    Item isotope1 = getIsotope(name, String.valueOf(FuelManager.all().get(name).get(subType).getDefault().isotopes[0]), type);
                    Item isotope2 = getIsotope(name, String.valueOf(FuelManager.all().get(name).get(subType).getDefault().isotopes[1]), type);
                    double radiation = ItemRadiation.byItem(isotope1)*isotope1Cnt + ItemRadiation.byItem(isotope2)*isotope2Cnt;
                    add(FissionFuel.NC_FUEL.get(List.of("fuel", name, subType, type)).get(), radiation/2);
                }
            }
        }

        for (String name: FuelManager.all().keySet()) {
            for(String subType: FuelManager.all().get(name).keySet()) {
                for(String type: List.of("", "ox", "ni", "za", "tr")) {
                    int isotope1Cnt = 1;
                    int isotope2Cnt = 8;
                    if(subType.substring(0,1).equalsIgnoreCase("h")) {
                        isotope1Cnt = 3;
                        isotope2Cnt = 6;
                    }
                    Item isotope1 = getIsotope(name, String.valueOf(FuelManager.all().get(name).get(subType).getDefault().isotopes[0]), type);
                    Item isotope2 = getIsotope(name, String.valueOf(FuelManager.all().get(name).get(subType).getDefault().isotopes[1]), type);
                    double radiation = ItemRadiation.byItem(isotope1)*isotope1Cnt + ItemRadiation.byItem(isotope2)*isotope2Cnt;
                    add(FissionFuel.NC_DEPLETED_FUEL.get(List.of("depleted", name, subType, type)).get(), radiation/1.5);
                }
            }
        }
    }

    public static Item getIsotope(String name, String id, String type)
    {
        if(!type.isEmpty()) {
            type = "_"+type;
        }
        if(!FissionFuel.NC_ISOTOPES.containsKey(name+"/"+id+type)) {
            for(String isotope: FissionFuel.NC_ISOTOPES.keySet()) {
                if(isotope.contains(id)) {
                    return  FissionFuel.NC_ISOTOPES.get(isotope).get();
                }
            }
        }
        return FissionFuel.NC_ISOTOPES.get(name+"/"+id+type).get();
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

    public static Item getItemByName(String name)
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
