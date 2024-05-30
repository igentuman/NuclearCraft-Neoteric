package igentuman.nc.radiation;

import igentuman.nc.content.fuel.FuelManager;
import igentuman.nc.content.materials.Materials;
import igentuman.nc.setup.registration.FissionFuel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;

public class FluidRadiation {
    protected static HashMap<Fluid, Double> radiationMap = new HashMap<>();
    protected static boolean initialized = false;
    public static HashMap<Fluid, Double> get()
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
                    String key = "fuel_"+name +"_"+ subType+type;
                    add(key.replace("-","_"), radiation/2);
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
        Fluid toAdd = getFluidByName(item);
        if(toAdd.equals(FluidStack.EMPTY.getFluid())) {
            return;
        }
        radiationMap.put(toAdd, radiation);
    }

    public static void add(Fluid item, double radiation)
    {
        radiationMap.put(item, radiation);
    }

    protected static Fluid getFluidByName(String name)
    {
        if(!name.contains(":")) {
            name = MODID +":" + name;
        }
        ResourceLocation itemKey = new ResourceLocation(name.replace("/", "_"));
        return ForgeRegistries.FLUIDS.getValue(itemKey);
    }

    public static double byFluid(Fluid item) {
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
