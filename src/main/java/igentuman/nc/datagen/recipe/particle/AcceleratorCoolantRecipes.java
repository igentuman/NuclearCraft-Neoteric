package igentuman.nc.datagen.recipe.particle;

import igentuman.nc.recipe.FluidOutput;
import igentuman.nc.recipe.particle.AcceleratorCoolantRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.datagen.recipe.ModRecipeProvider.fluidOf;

/** Generates linear-accelerator coolant recipes: an input coolant fluid converted to a hot output, removing heat. */
public final class AcceleratorCoolantRecipes {

    public static final int EXPECTED_COUNT = 4;

    private AcceleratorCoolantRecipes() {
    }

    public static void generate(RecipeOutput out) {
        coolant(out, "water", Fluids.WATER, 100, fluidOf("steam"), 100, 1L);
        coolant(out, "technical_water", fluidOf("technical_water"), 100, fluidOf("high_pressure_steam"), 100, 1L);
        coolant(out, "liquid_nitrogen", fluidOf("liquid_nitrogen"), 1, fluidOf("nitrogen"), 1, 1000L);
        coolant(out, "liquid_helium", fluidOf("liquid_helium"), 1, fluidOf("helium"), 1, 1000L);
    }

    private static void coolant(RecipeOutput out, String name, Fluid in, int inAmount, Fluid output, int outAmount,
                                 long heatPerMb) {
        if (in == null || output == null) return;
        AcceleratorCoolantRecipe recipe = new AcceleratorCoolantRecipe(
                SizedFluidIngredient.of(in, inAmount), FluidOutput.of(output, outAmount), heatPerMb, null);
        out.accept(ResourceLocation.fromNamespaceAndPath(MODID, "accelerator_coolant/" + name), recipe, null);
    }
}
