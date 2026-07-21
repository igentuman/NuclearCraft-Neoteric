package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.recipe.FluidOutput;
import igentuman.nc.recipe.heat_exchanger.HeatExchangerRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.datagen.recipe.ModRecipeProvider.fluidOf;

public class HeatExchangerRecipes {

    private static final int AMOUNT = 10;

    public static void heatExchanger(RecipeOutput out) {
        hx(out, "hot_helium", fluidOf("hot_helium"), fluidOf("helium"), 400);
        hx(out, "exhaust_steam", fluidOf("exhaust_steam"), Fluids.WATER, 100);
        hx(out, "low_quality_steam", fluidOf("low_quality_steam"), fluidOf("technical_water"), 100);
        hx(out, "flibe_hot_molten_salt", fluidOf("flibe_hot_molten_salt"), fluidOf("flibe_molten_salt"), 1800);

        hx(out, "water", Fluids.WATER, fluidOf("steam"), -200);
        hx(out, "technical_water", fluidOf("technical_water"), fluidOf("high_pressure_steam"), -300);
    }

    private static void hx(RecipeOutput out, String name, Fluid in, Fluid output, int heat) {
        if (in == null || output == null) return;
        HeatExchangerRecipe recipe = new HeatExchangerRecipe(
                SizedFluidIngredient.of(in, AMOUNT), FluidOutput.of(output, AMOUNT), heat);
        out.accept(ResourceLocation.fromNamespaceAndPath(MODID, "heat_exchanger/" + name), recipe, null);
    }
}
