package igentuman.nc.compat;

import igentuman.nc.recipe.UniversalProcessorRecipe;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record ProcessorRecipeStacks(List<List<ItemStack>> itemInputs, List<List<FluidStack>> fluidInputs,
                                    List<ItemStack> itemOutputs, List<FluidStack> fluidOutputs) {

    public static ProcessorRecipeStacks of(UniversalProcessorRecipe recipe) {
        List<List<ItemStack>> itemInputs = new ArrayList<>();
        for (SizedIngredient input : recipe.getItemInputs()) {
            List<ItemStack> options = Arrays.stream(input.getItems()).filter(stack -> !stack.isEmpty()).toList();
            if (!options.isEmpty()) itemInputs.add(options);
        }
        List<List<FluidStack>> fluidInputs = new ArrayList<>();
        for (SizedFluidIngredient input : recipe.getFluidInputs()) {
            List<FluidStack> options = Arrays.stream(input.getFluids()).filter(stack -> !stack.isEmpty()).toList();
            if (!options.isEmpty()) fluidInputs.add(options);
        }
        List<ItemStack> itemOutputs = new ArrayList<>();
        for (int i = 0; i < recipe.getItemOutputs().size(); i++) {
            ItemStack output = recipe.getOutputStack(i);
            if (!output.isEmpty()) itemOutputs.add(output);
        }
        List<FluidStack> fluidOutputs = new ArrayList<>();
        for (int i = 0; i < recipe.getFluidOutputs().size(); i++) {
            FluidStack output = recipe.getOutputFluidStack(i);
            if (!output.isEmpty()) fluidOutputs.add(output);
        }
        return new ProcessorRecipeStacks(itemInputs, fluidInputs, itemOutputs, fluidOutputs);
    }

    public List<ItemStack> primaryItemInputs() {
        return itemInputs.stream().map(List::getFirst).toList();
    }

    public List<FluidStack> primaryFluidInputs() {
        return fluidInputs.stream().map(List::getFirst).toList();
    }
}
