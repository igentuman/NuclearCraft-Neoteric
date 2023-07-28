package igentuman.nc.datagen.recipes.recipes;

import net.minecraft.world.item.Item;

import static igentuman.nc.setup.registration.Fuel.NC_ISOTOPES;
import static igentuman.nc.setup.registration.NCItems.NC_DUSTS;

public class ProcessorRecipeProvider {

    public static Item dustItem(String name)
    {
        if(NC_DUSTS.get(name) == null) {
            System.out.println("null dust: " + name);
        }
        return NC_DUSTS.get(name).get();
    }

    public static Item isotopeItem(String name)
    {
        if(NC_ISOTOPES.get(name) == null) {
            System.out.println("null isotope: " + name);
        }
        return NC_ISOTOPES.get(name).get();
    }
}
