package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.recipe.FluidOutput;
import igentuman.nc.recipe.fission.BoilingRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import static igentuman.nc.Main.MODID;
import static igentuman.nc.datagen.recipe.ModRecipeProvider.fluidOf;

public class FissionBoilingRecipes {

    public static void fissionBoiling(RecipeOutput out) {
        boil(out, "water_steam", Fluids.WATER, 10, fluidOf("steam"), 10, 100);
        boil(out, "helium_hot_helium", fluidOf("helium"), 10, fluidOf("hot_helium"), 10, 400);
    }

    private static void boil(RecipeOutput out, String name, Fluid in, int inAmount, Fluid output, int outAmount, int heatRequired) {
        if (in == null || output == null) return;
        BoilingRecipe recipe = new BoilingRecipe(
                SizedFluidIngredient.of(in, inAmount), FluidOutput.of(output, outAmount), heatRequired);
        out.accept(ResourceLocation.fromNamespaceAndPath(MODID, "fission_boiling/" + name), recipe, null);
    }
}
