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
import static igentuman.nc.setup.registration.NCItems.NC_ITEMS;
import static net.minecraft.world.level.material.Fluids.WATER;

public class FluidEnricherRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        FluidEnricherRecipes.consumer = consumer;
        ID = Processors.FLUID_ENRICHER;

        add(
                fluidIngredient(Materials.potassium_hydroxide, 4000),
                dustIngredient(Materials.iodine),
                fluidIngredient(Materials.potassium_iodide, 144)
        );

        add(
                fluidIngredient("minecraft:water", 5000),
                dustIngredient(Materials.coal),
                fluidIngredient("technical_water", 5000)
        );

        add(
                fluidIngredient("minecraft:water", 500),
                ingredient(NC_ITEMS.get("salt").get(), 3),
                fluidIngredient("chlorine", 500)
        );

        add(
                fluidIngredient("oxygen", 144),
                dustIngredient(Materials.uranium),
                fluidIngredient("uranium_oxide", 432)
        );

        add(
                fluidIngredient("minecraft:water", 1000),
                dustIngredient(Materials.boron_nitride),
                fluidIngredient("boron_nitride_solution", 250)
        );

        add(
                fluidIngredient("minecraft:water", 1000),
                dustIngredient(Materials.fluorite),
                fluidIngredient("fluorite_water", 250)
        );

        add(
                fluidIngredient("minecraft:water", 1000),
                dustIngredient(Materials.calcium_sulfate),
                fluidIngredient("calcium_sulfate_solution", 250)
        );

        add(
                fluidIngredient("minecraft:water", 1000),
                dustIngredient(Materials.sodium_fluoride),
                fluidIngredient("sodium_fluoride_solution", 250)
        );

        add(
                fluidIngredient("minecraft:water", 1000),
                dustIngredient(Materials.potassium_fluoride),
                fluidIngredient("potassium_fluoride_solution", 250)
        );

        add(
                fluidIngredient("minecraft:water", 1000),
                dustIngredient(Materials.sodium_hydroxide),
                fluidIngredient("sodium_hydroxide_solution", 250)
        );

        add(
                fluidIngredient("minecraft:water", 1000),
                dustIngredient(Materials.borax),
                fluidIngredient("borax_solution", 250)
        );

        add(
                fluidIngredient("minecraft:water", 1000),
                dustIngredient(Materials.irradiated_borax),
                fluidIngredient("irradiated_borax_solution", 250)
        );

        add(
                fluidIngredient("ethanol", 250),
                ingredient(MUSHROOM_ITEM.get(), 3),
                fluidIngredient("radaway", 250)
        );

        add(
                fluidIngredient("redstone_ethanol", 250),
                ingredient(MUSHROOM_ITEM.get(), 3),
                fluidIngredient("radaway_slow", 250)
        );
    }

    protected static void add(FluidStackIngredient inputFluid, NcIngredient inputItem, FluidStackIngredient output, double...modifiers) {
        itemsAndFluids(List.of(inputItem), new ArrayList<>(), List.of(inputFluid), List.of(output), modifiers);
    }
}
