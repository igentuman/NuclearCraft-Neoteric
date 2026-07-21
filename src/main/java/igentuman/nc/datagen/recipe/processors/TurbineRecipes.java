package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.recipe.FluidOutput;
import igentuman.nc.recipe.turbine.TurbineRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.datagen.recipe.ModRecipeProvider.fluidOf;

public class TurbineRecipes {

    public static void turbine(RecipeOutput out) {
        recipe(out, "steam", fluidOf("steam"), fluidOf("exhaust_steam"), 0.75);
        recipe(out, "high_pressure_steam", fluidOf("high_pressure_steam"), fluidOf("low_pressure_steam"), 1.0);
        recipe(out, "low_pressure_steam", fluidOf("low_pressure_steam"), fluidOf("low_quality_steam"), 0.1);
    }

    private static void recipe(RecipeOutput out, String name, Fluid in, Fluid output, double powerModifier) {
        if (in == null || output == null) return;
        TurbineRecipe recipe = new TurbineRecipe(
                SizedFluidIngredient.of(in, 10), FluidOutput.of(output, 10), powerModifier);
        out.accept(ResourceLocation.fromNamespaceAndPath(MODID, "turbine/" + name), recipe, null);
    }
}
