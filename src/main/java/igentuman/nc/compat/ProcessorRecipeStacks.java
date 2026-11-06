package igentuman.nc.compat;

import igentuman.nc.recipes.type.NcRecipe;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public record ProcessorRecipeStacks(List<ItemStack> primaryItemInputs, List<FluidStack> primaryFluidInputs,
                                    List<ItemStack> itemOutputs, List<FluidStack> fluidOutputs) {
    public static ProcessorRecipeStacks of(NcRecipe recipe) {
        List<ItemStack> items = new ArrayList<>();
        for (var ingredient : recipe.getInputItems()) {
            if (!ingredient.getRepresentations().isEmpty()) items.add(ingredient.getRepresentations().get(0).copy());
        }
        List<FluidStack> fluids = new ArrayList<>();
        for (var ingredient : recipe.getInputFluids()) {
            if (!ingredient.getRepresentations().isEmpty()) fluids.add(ingredient.getRepresentations().get(0).copy());
        }
        return new ProcessorRecipeStacks(items, fluids, recipe.getResultItems(), recipe.getOutputFluids());
    }
}
