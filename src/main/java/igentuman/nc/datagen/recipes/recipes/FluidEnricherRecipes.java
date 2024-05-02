package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.materials.Materials;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.NcIngredient;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.setup.registration.NCBlocks.MUSHROOM_ITEM;

public class FluidEnricherRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        FluidEnricherRecipes.consumer = consumer;
        ID = Processors.FLUID_ENRICHER;

        add(
                fluidIngredient(Materials.potassium_hydroxide, 4000),
                dustIngredient(Materials.iodine),
                fluidStack(Materials.potassium_iodide, 144)
        );

        add(
                fluidIngredient("minecraft:water", 5000),
                dustIngredient(Materials.coal),
                fluidStack("technical_water", 5000)
        );

        add(
                fluidIngredient("oxygen", 100),
                dustIngredient(Materials.uranium),
                fluidStack("uranium_oxide", 432)
        );

        add(
                fluidIngredient("minecraft:water", 1000),
                dustIngredient(Materials.boron_nitride),
                fluidStack("boron_nitride_solution", 250)
        );

        add(
                fluidIngredient("minecraft:water", 1000),
                dustIngredient(Materials.fluorite),
                fluidStack("fluorite_water", 250)
        );

        add(
                fluidIngredient("minecraft:water", 1000),
                dustIngredient(Materials.calcium_sulfate),
                fluidStack("calcium_sulfate_solution", 250)
        );

        add(
                fluidIngredient("minecraft:water", 1000),
                dustIngredient(Materials.sodium_fluoride),
                fluidStack("sodium_fluoride_solution", 250)
        );

        add(
                fluidIngredient("minecraft:water", 1000),
                dustIngredient(Materials.potassium_fluoride),
                fluidStack("potassium_fluoride_solution", 250)
        );

        add(
                fluidIngredient("minecraft:water", 1000),
                dustIngredient(Materials.sodium_hydroxide),
                fluidStack("sodium_hydroxide_solution", 250)
        );

        add(
                fluidIngredient("minecraft:water", 1000),
                dustIngredient(Materials.borax),
                fluidStack("borax_solution", 250)
        );

        add(
                fluidIngredient("minecraft:water", 1000),
                dustIngredient(Materials.irradiated_borax),
                fluidStack("irradiated_borax_solution", 250)
        );

        add(
                fluidIngredient("ethanol", 250),
                ingredient(MUSHROOM_ITEM.get(), 3),
                fluidStack("radaway", 250)
        );

        add(
                fluidIngredient("redstone_ethanol", 250),
                ingredient(MUSHROOM_ITEM.get(), 3),
                fluidStack("radaway_slow", 250)
        );
    }

    protected static void add(FluidStackIngredient inputFluid, NcIngredient inputItem, FluidStack output, double...modifiers) {
        itemsAndFluids(List.of(inputItem), new ArrayList<>(), List.of(inputFluid), List.of(output), modifiers);
    }
}
