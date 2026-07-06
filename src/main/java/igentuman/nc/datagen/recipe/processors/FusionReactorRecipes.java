package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.recipe.FluidOutput;
import igentuman.nc.recipe.fusion.FusionCoolantRecipe;
import igentuman.nc.recipe.fusion.FusionRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.datagen.recipe.ModRecipeProvider.MOLTEN_INGOT;
import static igentuman.nc.datagen.recipe.ModRecipeProvider.fluidOf;
import static igentuman.nc.datagen.recipe.ModRecipeProvider.isotopeFluid;

/**
 * Fusion reactor fuel + coolant process recipes. {@code energy} and {@code temperature} are ported
 * verbatim from NuclearCraft Neoteric ({@code temperature} scaled to the raw optimal plasma temp by
 * x1_000_000); {@code processTime = timeModifier x 10}. Radiation is dropped.
 */
public class FusionReactorRecipes {

    public static void fusion(RecipeOutput out) {
        Fluid deuterium = fluidOf("deuterium");
        Fluid tritium = fluidOf("tritium");
        Fluid helium = fluidOf("helium");
        Fluid helium3 = fluidOf("helium_3");
        Fluid hydrogen = fluidOf("hydrogen");
        Fluid lithium6 = isotopeFluid("lithium/6", "");
        Fluid lithium7 = isotopeFluid("lithium/7", "");
        Fluid quantite = isotopeFluid("quantite", "");
        Fluid xenorium = isotopeFluid("xenorium/298", "");

        fuel(out, "deuterium_tritium", deuterium, 250, tritium, 250, one(helium, 65, 4), 20, 220000, 816);
        fuel(out, "deuterium_hydrogen", deuterium, 1000, hydrogen, 1000, one(helium3, 250, 4), 15, 220000, 1245);
        fuel(out, "hydrogen_tritium", hydrogen, 1000, tritium, 1000, one(helium3, 250, 4), 20, 380000, 6050);
        fuel(out, "hydrogen_helium3", hydrogen, 1000, helium3, 1000, one(helium, 250, 4), 20, 300000, 3339);
        fuel(out, "hydrogen_lithium6", hydrogen, 1000, lithium6, MOLTEN_INGOT, mix(tritium, 500, 2, helium, 500, 2), 35, 270200, 7278);
        fuel(out, "hydrogen_lithium7", hydrogen, 1000, lithium7, MOLTEN_INGOT, one(helium, 500, 4), 40, 266000, 5071);
        fuel(out, "hydrogen_quantite", hydrogen, 1000, quantite, MOLTEN_INGOT, one(helium, 750, 4), 60, 568800, 16370);
        fuel(out, "deuterium_helium3", deuterium, 1000, helium3, 1000, mix(hydrogen, 500, 2, helium, 500, 2), 20, 301400, 1156);
        fuel(out, "deuterium_lithium6", deuterium, 1000, lithium6, MOLTEN_INGOT, one(helium, 500, 4), 25, 450000, 2632);
        fuel(out, "deuterium_lithium7", deuterium, 1000, lithium7, MOLTEN_INGOT, one(helium, 500, 4), 45, 458000, 5034);
        fuel(out, "deuterium_quantite", deuterium, 1000, quantite, MOLTEN_INGOT, one(helium, 1500, 2), 65, 452200, 16883);
        fuel(out, "tritium_helium3", tritium, 1000, helium3, 1000, one(helium, 250, 4), 30, 418200, 2604);
        fuel(out, "tritium_lithium6", tritium, 1000, lithium6, MOLTEN_INGOT, one(helium, 1000, 2), 45, 383000, 4971);
        fuel(out, "tritium_lithium7", tritium, 1000, lithium7, MOLTEN_INGOT, one(helium, 1000, 2), 50, 287000, 5511);
        fuel(out, "tritium_quantite", tritium, 1000, quantite, MOLTEN_INGOT, one(helium, 1500, 2), 70, 800000, 33215);
        fuel(out, "helium3_lithium6", helium3, 1000, lithium6, MOLTEN_INGOT, mix(hydrogen, 500, 2, helium, 1000, 2), 45, 530000, 9506);
        fuel(out, "helium3_lithium7", helium3, 1000, lithium7, MOLTEN_INGOT, mix(deuterium, 500, 2, helium, 1000, 2), 50, 445400, 9673);
        fuel(out, "helium3_quantite", helium3, 1000, quantite, MOLTEN_INGOT, mix(deuterium, 500, 2, helium, 1500, 2), 70, 1148000, 29574);
        fuel(out, "lithium6_xenorium", lithium6, MOLTEN_INGOT * 4, xenorium, MOLTEN_INGOT * 4, one(helium, 3000, 2), 65, 510400, 14536);
        fuel(out, "lithium6_quantite", lithium6, MOLTEN_INGOT * 7, quantite, MOLTEN_INGOT * 7, one(helium, 2000, 2), 85, 1231400, 37048);
        fuel(out, "lithium7_quantite", lithium7, MOLTEN_INGOT * 9, quantite, MOLTEN_INGOT * 9, one(helium, 2000, 2), 90, 1400000, 202000);

        coolant(out, "liquid_nitrogen", fluidOf("liquid_nitrogen"), 10, fluidOf("nitrogen"), 10, 2500);
        coolant(out, "liquid_helium", fluidOf("liquid_helium"), 10, fluidOf("helium"), 10, 2500);
        coolant(out, "water", Fluids.WATER, 1000, fluidOf("steam"), 1000, 2000);
        coolant(out, "technical_water", fluidOf("technical_water"), 1000, fluidOf("high_pressure_steam"), 1000, 2500);
    }

    private static void fuel(RecipeOutput out, String name, Fluid a, int aAmt, Fluid b, int bAmt,
                             List<FluidOutput> outputs, double timeModifier, int energy, double temperature) {
        if (a == null || b == null || outputs.isEmpty()) return;
        FusionRecipe recipe = new FusionRecipe(
                SizedFluidIngredient.of(a, aAmt),
                SizedFluidIngredient.of(b, bAmt),
                outputs, energy, temperature * 1_000_000, (int) Math.round(timeModifier * 10));
        out.accept(ResourceLocation.fromNamespaceAndPath(MODID, "fusion_reactor/" + name), recipe, null);
    }

    private static void coolant(RecipeOutput out, String name, Fluid in, int inAmt, Fluid output, int outAmt, int coolingRate) {
        if (in == null || output == null) return;
        FusionCoolantRecipe recipe = new FusionCoolantRecipe(
                SizedFluidIngredient.of(in, inAmt), FluidOutput.of(output, outAmt), coolingRate);
        out.accept(ResourceLocation.fromNamespaceAndPath(MODID, "fusion_coolant/" + name), recipe, null);
    }

    private static List<FluidOutput> one(Fluid fluid, int amount, int count) {
        List<FluidOutput> list = new ArrayList<>();
        if (fluid != null) {
            for (int i = 0; i < count; i++) list.add(FluidOutput.of(fluid, amount));
        }
        return list;
    }

    private static List<FluidOutput> mix(Fluid a, int aAmt, int aCount, Fluid b, int bAmt, int bCount) {
        List<FluidOutput> list = new ArrayList<>();
        if (a != null) for (int i = 0; i < aCount; i++) list.add(FluidOutput.of(a, aAmt));
        if (b != null) for (int i = 0; i < bCount; i++) list.add(FluidOutput.of(b, bAmt));
        return list;
    }
}
