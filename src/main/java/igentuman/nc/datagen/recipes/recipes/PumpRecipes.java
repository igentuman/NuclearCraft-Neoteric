package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.materials.Materials;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.NcIngredient;
import net.minecraft.data.IFinishedRecipe;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.setup.registration.NCItems.NC_ITEMS;
import static net.minecraft.fluid.Fluids.LAVA;
import static net.minecraft.fluid.Fluids.WATER;

public class PumpRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<IFinishedRecipe> consumer) {
        PumpRecipes.consumer = consumer;
        ID = Processors.PUMP;

        add(ingredient(NC_ITEMS.get("water_collector").get()), fluidStack(WATER, 200), 0.1D);
        add(ingredient(NC_ITEMS.get("compact_water_collector").get()), fluidStack(WATER, 2000), 0.1D);
        add(ingredient(NC_ITEMS.get("dense_water_collector").get()), fluidStack(WATER, 10000), 0.1D);

        add(ingredient(NC_ITEMS.get("nitrogen_collector").get()), fluidStack("nitrogen", 50), 0.1D);
        add(ingredient(NC_ITEMS.get("compact_nitrogen_collector").get()), fluidStack("nitrogen", 500), 0.1D);
        add(ingredient(NC_ITEMS.get("dense_nitrogen_collector").get()), fluidStack("nitrogen", 2500), 0.1D);

        add(ingredient(NC_ITEMS.get("helium_collector").get()), fluidStack("helium", 50), 0.1D);
        add(ingredient(NC_ITEMS.get("compact_helium_collector").get()), fluidStack("helium", 500), 0.1D);
        add(ingredient(NC_ITEMS.get("dense_helium_collector").get()), fluidStack("helium", 2500), 0.1D);

        add(ingredient(NC_ITEMS.get("lava_collector").get()), fluidStack(LAVA, 1000), 1D, 3D);
    }

    protected static void add(NcIngredient inputItem, FluidStack outputFluid, double...modifiers) {
        itemsAndFluids(Arrays.asList(inputItem), new ArrayList<>(), new ArrayList<>(), Arrays.asList(outputFluid), modifiers);
    }
}
