package igentuman.nc.compat;

import igentuman.nc.api.recipe.FluidOutput;
import igentuman.nc.api.recipe.ItemOutput;
import igentuman.nc.api.recipe.ProcessorRecipeView;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.context.ContextMap;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackContentsFactory;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.List;

/** Resolves native ingredient displays without discarding required components. */
public record ProcessorRecipeStacks(List<List<ItemStack>> itemInputs, List<List<FluidStack>> fluidInputs,
                                    List<ItemStack> itemOutputs, List<FluidStack> fluidOutputs) {

    public static ProcessorRecipeStacks of(ProcessorRecipeView recipe) {
        return of(recipe, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
    }

    public static ProcessorRecipeStacks of(ProcessorRecipeView recipe, RegistryAccess registries) {
        ContextMap context = new ContextMap.Builder()
                .withParameter(SlotDisplayContext.REGISTRIES, registries)
                .create(SlotDisplayContext.CONTEXT);
        List<List<ItemStack>> itemInputs = new ArrayList<>();
        for (SizedIngredient input : recipe.getItemInputs()) {
            List<ItemStack> options = input.ingredient().display().resolveForStacks(context).stream()
                    .filter(stack -> !stack.isEmpty()).map(stack -> stack.copyWithCount(input.count())).toList();
            if (!options.isEmpty()) itemInputs.add(options);
        }
        List<List<FluidStack>> fluidInputs = new ArrayList<>();
        for (SizedFluidIngredient input : recipe.getFluidInputs()) {
            List<FluidStack> options = input.ingredient().display().resolve(context, FluidStackContentsFactory.INSTANCE)
                    .filter(stack -> !stack.isEmpty()).map(stack -> stack.copyWithAmount(input.amount())).toList();
            if (!options.isEmpty()) fluidInputs.add(options);
        }
        List<ItemStack> itemOutputs = new ArrayList<>();
        for (ItemOutput entry : recipe.getItemOutputs()) {
            ItemStack output = entry.resolve();
            if (!output.isEmpty()) itemOutputs.add(output);
        }
        List<FluidStack> fluidOutputs = new ArrayList<>();
        for (FluidOutput entry : recipe.getFluidOutputs()) {
            FluidStack output = entry.resolve();
            if (!output.isEmpty()) fluidOutputs.add(output);
        }
        return new ProcessorRecipeStacks(itemInputs, fluidInputs, itemOutputs, fluidOutputs);
    }

    public boolean resolvesAllSlots(ProcessorRecipeView recipe) {
        return itemInputs.size() == recipe.getItemInputs().size()
                && fluidInputs.size() == recipe.getFluidInputs().size()
                && itemOutputs.size() == recipe.getItemOutputs().size()
                && fluidOutputs.size() == recipe.getFluidOutputs().size();
    }

    public List<ItemStack> primaryItemInputs() {
        return itemInputs.stream().map(List::getFirst).toList();
    }

    public List<FluidStack> primaryFluidInputs() {
        return fluidInputs.stream().map(List::getFirst).toList();
    }
}
