package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.fuel.FuelManager;
import igentuman.nc.content.materials.Materials;
import igentuman.nc.content.materials.NCMaterial;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.setup.registration.Fuel;
import igentuman.nc.setup.registration.NCItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.setup.registration.NCItems.ALL_NC_ITEMS;
import static net.minecraft.world.item.Items.OBSIDIAN;
import static net.minecraft.world.item.Items.SUGAR;

public class ExtractorRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        ExtractorRecipes.consumer = consumer;
        ID = Processors.EXTRACTOR;
        add(ingredient(ALL_NC_ITEMS.get("ground_cocoa_nibs").get()), ingredient(ALL_NC_ITEMS.get("cocoa_solids").get()), fluidStack("cocoa_butter", 144));

    }

    protected static void add(NcIngredient inputItem, NcIngredient outputItem, FluidStack outputFluid, double...modifiers) {
        itemsAndFluids(List.of(inputItem), List.of(outputItem), new ArrayList<>(), List.of(outputFluid), modifiers);
    }
}
