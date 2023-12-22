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
import static igentuman.nc.util.DataGenUtil.forgeOre;

public class LeacherRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        LeacherRecipes.consumer = consumer;
        ID = Processors.LEACHER;

        for(String material: Materials.slurries()) {
            add(
                    fluidIngredient("aqua_regia_acid", 250),
                    ingredient(forgeOre(material)),
                    fluidStack(material+"_slurry", 1000)
            );
        }
    }

    protected static void add(FluidStackIngredient inputFluid, NcIngredient inputItem, FluidStack output, double...modifiers) {
        itemsAndFluids(List.of(inputItem), new ArrayList<>(), List.of(inputFluid), List.of(output), modifiers);
    }
}
