package igentuman.nc.datagen.recipes.recipes;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import static igentuman.nc.setup.registration.Fuel.NC_ISOTOPES;
import static igentuman.nc.setup.registration.NCItems.*;

public class ProcessorRecipeProvider {

    public static Item dustItem(String name)
    {
        if(NC_DUSTS.get(name) == null) {
            System.out.println("null dust: " + name);
        }
        return NC_DUSTS.get(name).get();
    }

    public static TagKey<Item> dustTag(String name)
    {
        if(DUSTS_TAG.get(name) == null) {
            System.out.println("null dust tag: " + name);
        }
        return DUSTS_TAG.get(name);
    }

    public static Item isotopeItem(String name)
    {
        if(NC_ISOTOPES.get(name) == null) {
            System.out.println("null isotope: " + name);
        }
        return NC_ISOTOPES.get(name).get();
    }

    public static Item plateItem(String name)
    {
        if(NC_PLATES.get(name) == null) {
            System.out.println("null plate: " + name);
        }
        return NC_PLATES.get(name).get();
    }

    public static TagKey<Item> plateTag(String name)
    {
        if(PLATES_TAG.get(name) == null) {
            System.out.println("null plate tag: " + name);
        }
        return PLATES_TAG.get(name);
    }

    public static Item ingotItem(String name)
    {
        if(NC_INGOTS.get(name) == null) {
            System.out.println("null ingot: " + name);
        }
        return NC_INGOTS.get(name).get();
    }

    public static TagKey<Item> ingotTag(String name)
    {
        if(INGOTS_TAG.get(name) == null) {
            System.out.println("null ingot tag: " + name);
        }
        return INGOTS_TAG.get(name);
    }

    public static TagKey<Item> gemTag(String name) {
        if(GEMS_TAG.get(name) == null) {
            System.out.println("null gem tag: " + name);
        }
        return GEMS_TAG.get(name);
    }

    public static Item gemItem(String name)
    {
        if(NC_GEMS.get(name) == null) {
            System.out.println("null gem: " + name);
        }
        return NC_GEMS.get(name).get();
    }


}
