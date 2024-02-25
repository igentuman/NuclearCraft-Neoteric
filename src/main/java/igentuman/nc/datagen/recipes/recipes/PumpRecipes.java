package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.NcIngredient;
import net.minecraft.data.recipes.FinishedRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import static igentuman.nc.setup.registration.NCItems.NC_ITEMS;

public class PumpRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        PumpRecipes.consumer = consumer;
        ID = Processors.PUMP;

        add(ingredient(NC_ITEMS.get("water_collector").get()), fluidIngredient("minecraft:water", 200), 0.1D);
        add(ingredient(NC_ITEMS.get("compact_water_collector").get()), fluidIngredient("minecraft:water", 2000), 0.1D);
        add(ingredient(NC_ITEMS.get("dense_water_collector").get()), fluidIngredient("minecraft:water", 10000), 0.1D);

        add(ingredient(NC_ITEMS.get("nitrogen_collector").get()), fluidIngredient("nitrogen", 50), 0.1D);
        add(ingredient(NC_ITEMS.get("compact_nitrogen_collector").get()), fluidIngredient("nitrogen", 500), 0.1D);
        add(ingredient(NC_ITEMS.get("dense_nitrogen_collector").get()), fluidIngredient("nitrogen", 2500), 0.1D);

        add(ingredient(NC_ITEMS.get("helium_collector").get()), fluidIngredient("helium", 50), 0.1D);
        add(ingredient(NC_ITEMS.get("compact_helium_collector").get()), fluidIngredient("helium", 500), 0.1D);
        add(ingredient(NC_ITEMS.get("dense_helium_collector").get()), fluidIngredient("helium", 2500), 0.1D);

        add(ingredient(NC_ITEMS.get("lava_collector").get()), fluidIngredient("minecraft:lava", 1000), 1D, 3D);
    }

    protected static void add(NcIngredient inputItem, FluidStackIngredient outputFluid, double...modifiers) {
        itemsAndFluids(List.of(inputItem), new ArrayList<>(), new ArrayList<>(), List.of(outputFluid), modifiers);
    }
}
